package org.openhab.binding.mysqueezebox.internal;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySqueezeboxClient
{
	private static final String MYSQUEEZEBOX_LOGIN_URL = "https://mysqueezebox.com/user/login";
	private static final String MYSQUEEZEBOX_RPC_URL = "https://mysqueezebox.com/jsonrpc.js";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MySqueezeboxClient.class);
	private String email = null;
	private String password = null;
	private String playerId = null;
	private HttpClient httpClient = null;

	public MySqueezeboxClient(String email, String password, String playerId)
			throws Exception
	{
		SSLContext sslcontext = SSLContext.getInstance("SSL");
		sslcontext.init(null, new TrustManager[] { new NaiveX509TrustManager() },
				null);
		SSLSocketFactory socketFactory = new SSLSocketFactory(sslcontext,
				SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("https", 443, socketFactory));

		this.email = email;
		this.password = password;
		this.playerId = playerId;
		this.httpClient = new DefaultHttpClient(
				new SingleClientConnManager(registry));
	}

	public void login() throws Exception
	{
		HttpPost request = new HttpPost(MySqueezeboxClient.MYSQUEEZEBOX_LOGIN_URL);
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("email", this.email));
		postParameters.add(new BasicNameValuePair("password", this.password));
		request.setEntity(new UrlEncodedFormEntity(postParameters));

		HttpResponse response = httpClient.execute(request);
		EntityUtils.consume(response.getEntity());
	}

	public JSONObject command(String[] command, int retriesLeft) throws Exception
	{
		try
		{
			return this.command(command);
		}
		catch (Exception e)
		{
			retriesLeft--;
			if (retriesLeft > 0)
			{
				MySqueezeboxClient.LOGGER
						.warn("MySqueezebox command \"" + ArrayUtils.toString(command)
								+ "\" failed. Retries left: " + retriesLeft);
				try
				{
					// Pause 10 seconds before next retry
					Thread.sleep(10000);
				}
				catch (InterruptedException ex)
				{
					Thread.currentThread().interrupt();
				}
				return this.command(command, retriesLeft);
			}
			else
			{
				throw e;
			}
		}
	}

	public JSONObject command(String[] command) throws Exception
	{
		HttpPost request = new HttpPost(MySqueezeboxClient.MYSQUEEZEBOX_RPC_URL);
		request.setHeader("Content-Type", "application/json");
		StringBuilder query = new StringBuilder();
		query.append("{\"id\": 1, \"method\": \"slim.request\", \"params\":[\"");
		query.append(this.playerId).append("\", [\"");
		query.append(StringUtils.join(command, "\",\""));
		query.append("\"]]}");
		HttpEntity entity = new StringEntity(query.toString(), "UTF-8");
		request.setEntity(entity);

		HttpResponse response = httpClient.execute(request);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK)
		{
			throw new Exception("MySqueezebox.com command failed: "
					+ IOUtils.toString(response.getEntity().getContent()));
		}

		JSONObject json = new JSONObject(
				IOUtils.toString(response.getEntity().getContent()));
		EntityUtils.consume(response.getEntity());
		if (!json.isNull("error"))
		{
			throw new Exception(
					"MySqueezebox command failed - Error: " + json.getString("error"));
		}

		return json;
	}
}

class NaiveX509TrustManager implements X509TrustManager
{
	@Override
	public void checkClientTrusted(X509Certificate[] certs, String str)
			throws CertificateException
	{
	}

	@Override
	public void checkServerTrusted(X509Certificate[] certs, String str)
			throws CertificateException
	{
	}

	@Override
	public X509Certificate[] getAcceptedIssuers()
	{
		return null;
	}
}

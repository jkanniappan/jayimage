package com.imageTest.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imageTest.Util.Constant;

public class ImageDownloader {
	
	private static CloseableHttpClient httpClient = null;
	private static PoolingHttpClientConnectionManager connectionManager;
	private static Constant constant;
	private static List<String> earthDates;
	private static List<NameValuePair> nvps;
	
	private static Logger logger = LoggerFactory.getLogger(ImageDownloader.class);

	public static void main(String args[]) {
		try {
			constant = new Constant();
			earthDates = constant.getEarthDates();
			logger.debug("earthDates : {} ", earthDates);

			httpClient = ConnectService();
			ExecutorService executor = Executors.newFixedThreadPool(10);
			for (String earthDate : earthDates) {
				nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("api_key", "DEMO_KEY"));
				//nvps.add(new BasicNameValuePair("camera", "NAVCAM"));
				nvps.add(new BasicNameValuePair("earth_date", earthDate));
				Runnable worker = new EarthDatePictureThread(constant, httpClient, nvps);
				executor.execute(worker);
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			releaseResources();
		} catch (Exception e) {
			logger.error("Error downloading images .", e, e);
		}
	}

	/**
	 * get http connection, trust all certificates
	 * @return
	 */
	public static CloseableHttpClient ConnectService() {
		TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}
		} };

		try {
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustManager, new java.security.SecureRandom());
			SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext,
					NoopHostnameVerifier.INSTANCE);

			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.INSTANCE)
					.register("https", connectionFactory).build();

			connectionManager = new PoolingHttpClientConnectionManager(registry);
			HttpHost host = new HttpHost(constant.getHostUrl(), 443);
			connectionManager.setMaxPerRoute(new HttpRoute(host), 5);
			connectionManager.setMaxTotal(10);

			httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
			logger.debug(" httpClient created :[{}]", httpClient.toString());
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			logger.error("Error getting httpClient : ", e.getMessage());
		}
		return httpClient;
	}

	
	
	/** 
	 * release resources
	 */
	private static void releaseResources() {
		try {
			if (null != httpClient) {
				httpClient.close();
				logger.info("Release Resources (httpClient) finally");	
			}
			if (null != connectionManager) {
				connectionManager.close();
				logger.info("Release Resources (connectionManager) finally");	
			}
			logger.info("Release Resources finally");	
		} catch (IOException e) {
			logger.error("Error while Releasing Resources : ", e.getMessage());
		}
	}

}

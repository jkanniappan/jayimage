package com.imageTest.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.imageTest.Util.Constant;
import com.imageTest.entity.EarthDatePhotos;
import com.imageTest.entity.EarthDatePhotos.Photo;

public class EarthDatePictureThread implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(EarthDatePictureThread.class);

	private Constant constant;
	private CloseableHttpClient httpClient;
	private List<NameValuePair> nvps;
	

	public EarthDatePictureThread(Constant constant, CloseableHttpClient httpClient,
			List<NameValuePair> nvps) {
		super();
		this.constant = constant;
		this.httpClient = httpClient;
		this.nvps = nvps;
	}


	@Override
	public void run() {
		logger.info(Thread.currentThread().getName()+" Started.");		
		getImagesForEarthDate(httpClient, nvps);
		logger.info(Thread.currentThread().getName()+" End.");
	}

	
	/**
	 * get Images for the given date
	 * @param httpClient
	 * @param nvps
	 * @throws Exception
	 */
	private void getImagesForEarthDate(CloseableHttpClient httpClient, List<NameValuePair> nvps) {
		try {
			Constant.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			URI marsUri = new URIBuilder(constant.getHostUrl()).addParameters(nvps).build();
			HttpGet httpGet = new HttpGet(marsUri);
			httpGet.addHeader("accept", "application/json");
			logger.debug("Get Image Request :[{}]", httpGet.getRequestLine());

			HttpResponse response = httpClient.execute(httpGet);
			logger.debug("Get Image Response:[{}]", response);

			if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
				HttpEntity entity = response.getEntity();
				List<Photo> earthPhotos = null;
				if (null != entity) {
					String jsonString = EntityUtils.toString(entity);
					logger.debug("EntityUtils.toString(entity) : {} ", jsonString);
					EarthDatePhotos photos = Constant.getObjectMapper().readValue(jsonString, EarthDatePhotos.class);
					logger.debug("photos : {}", photos);
					earthPhotos = photos.getPhotos();
					logger.debug("number of images received : {} ", earthPhotos.size());
					/*for (Photo photo : earthPhotos) {
						logger.debug("photo getImgSrc :{}", photo.getImgSrc());
						logger.debug("photo getId :{}", photo.getId());
					}*/
				}
				EntityUtils.consume(entity);
				if (earthPhotos != null && !earthPhotos.isEmpty()) {
					logger.debug("number of images to Download: {} ", earthPhotos.size());
					downloadImages(httpClient, earthPhotos);
				}
			}
		} catch (Exception e) {
			logger.error("Error getting Images For EarthDate.", e, e);
		}
	}
	
	/**
	 * storing all the images in the same location as the images are having unique name
	 * @param httpClient
	 * @param earthPhotos
	 * @throws Exception
	 */
	private void downloadImages(CloseableHttpClient httpClient, List<Photo> earthPhotos) throws Exception {
		InputStream is = null;
		FileOutputStream fos = null;
		URI marsUriToDownload = null;
		HttpGet httpGet = null;
		try {
			for (Photo photo : earthPhotos) {
				logger.debug("Image To Download  :[{}]", photo.getImgSrc());
				marsUriToDownload = new URIBuilder(photo.getImgSrc()).build();
				httpGet = new HttpGet(marsUriToDownload);
				logger.debug("Get download Request :[{}]", httpGet.getRequestLine());

				HttpResponse response = httpClient.execute(httpGet);
				logger.debug("Image download Response:[{}]", response);
				
				HttpEntity entity = response.getEntity();
				int responseCode = response.getStatusLine().getStatusCode();
				logger.debug("Response Code: " + responseCode);
				
				is = entity.getContent();

				String filePath = String.format("%s/%s", constant.getLocalPathToDownload(),
						FilenameUtils.getName(photo.getImgSrc()));
				fos = new FileOutputStream(new File(filePath));
				
				int inByte;
				while ((inByte = is.read()) != -1) {
					fos.write(inByte);
				}
				EntityUtils.consume(entity);
			}
			logger.info("Images downloaded successfully..");
		} finally {
			is.close();
			fos.close();
			logger.info("Images stream closed finally");
		}
	}
	
	@Override
	public String toString() {
		return "EarthDatePictureThread [getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}

}

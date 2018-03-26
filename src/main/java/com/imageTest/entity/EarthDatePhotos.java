package com.imageTest.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class EarthDatePhotos {

	private List<Photo> photos;

    public List<Photo> getPhotos() {
        return photos;
    }

	@Override
	public String toString() {
		return "EarthDatePhotos [photos=" + photos + "]";
	}

	public static final class Photo {
		@Override
		public String toString() {
			return "Photo [earthDate=" + earthDate + ", id=" + id + ", imgSrc=" + imgSrc + "]";
		}
		public String getEarthDate() {
			return earthDate;
		}
		public void setEarthDate(String earthDate) {
			this.earthDate = earthDate;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getImgSrc() {
			return imgSrc;
		}
		public void setImgSrc(String imgSrc) {
			this.imgSrc = imgSrc;
		}
		@JsonProperty("earth_date")
		private String earthDate;
		private String id;
		@JsonProperty("img_src")
		private String imgSrc;

		
	}
}

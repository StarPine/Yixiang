package entity;

import java.sql.Timestamp;

public class MerchantVideo {
	
	// Fields

		private Integer videoId;
		private Integer merchantId;
		private String cover;
		private String path;
		private String duaration;
		private String topPosition;

		// Constructors

		/** default constructor */
		public MerchantVideo() {
		}

		/** minimal constructor */
		public MerchantVideo(Integer merchantId) {
			this.merchantId = merchantId;
		}

		/** full constructor */
		public MerchantVideo(Integer merchantId, String cover, String path,
				String duaration, String topPosition) {
			this.merchantId = merchantId;
			this.cover = cover;
			this.path = path;
			this.duaration = duaration;
			this.topPosition = topPosition;
		}

		// Property accessors

		public Integer getVideoId() {
			return this.videoId;
		}

		public void setVideoId(Integer videoId) {
			this.videoId = videoId;
		}

		public Integer getMerchantId() {
			return this.merchantId;
		}

		public void setMerchantId(Integer merchantId) {
			this.merchantId = merchantId;
		}

		public String getCover() {
			return this.cover;
		}

		public void setCover(String cover) {
			this.cover = cover;
		}

		public String getPath() {
			return this.path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getDuaration() {
			return this.duaration;
		}

		public void setDuaration(String duaration) {
			this.duaration = duaration;
		}

		public String getTopPosition() {
			return this.topPosition;
		}

		public void setTopPosition(String topPosition) {
			this.topPosition = topPosition;
		}


	
}

package entity;

import java.sql.Timestamp;

public class DrawingInfo {

	// Fields

		private String drawingId;
		private String userId;
		private String drawingImg;
		private String drawingVideo;
		private String description;
		private Timestamp createDate;
		private Timestamp lastModify;
		private Integer clickNum;
		private String videoCover;

		
		
		// Constructors

		

		/** default constructor */
		public DrawingInfo() {
		}

		public String getVideoCover() {
			return videoCover;
		}

		public void setVideoCover(String videoCover) {
			this.videoCover = videoCover;
		}

		/** full constructor */
		public DrawingInfo(String userId, String drawingImg,
				String drawingVideo, String description, Timestamp createDate,
				Timestamp lastModify, Integer clickNum) {
			this.userId = userId;
			this.drawingImg = drawingImg;
			this.drawingVideo = drawingVideo;
			this.description = description;
			this.createDate = createDate;
			this.lastModify = lastModify;
			this.clickNum = clickNum;
		}

		// Property accessors

		public String getDrawingId() {
			return this.drawingId;
		}

		public void setDrawingId(String drawingId) {
			this.drawingId = drawingId;
		}

		public String getUserId() {
			return this.userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getDrawingImg() {
			return this.drawingImg;
		}

		public void setDrawingImg(String drawingImg) {
			this.drawingImg = drawingImg;
		}

		public String getDrawingVideo() {
			return this.drawingVideo;
		}

		public void setDrawingVideo(String drawingVideo) {
			this.drawingVideo = drawingVideo;
		}

		public String getDescription() {
			return this.description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Timestamp getCreateDate() {
			return this.createDate;
		}

		public void setCreateDate(Timestamp createDate) {
			this.createDate = createDate;
		}

		public Timestamp getLastModify() {
			return this.lastModify;
		}

		public void setLastModify(Timestamp lastModify) {
			this.lastModify = lastModify;
		}

		public Integer getClickNum() {
			return this.clickNum;
		}

		public void setClickNum(Integer clickNum) {
			this.clickNum = clickNum;
		}

}

package entity;

import java.util.Date;

public class Advertisement {

	// Fields

		private String id;
		private String image;
		private Date createDate;
		private Boolean isPlacing;
		private String address;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getImage() {
			return image;
		}
		public void setImage(String image) {
			this.image = image;
		}
		public Date getCreateDate() {
			return createDate;
		}
		public void setCreateDate(Date createDate) {
			this.createDate = createDate;
		}
		public Boolean getIsPlacing() {
			return isPlacing;
		}
		public void setIsPlacing(Boolean isPlacing) {
			this.isPlacing = isPlacing;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		
}

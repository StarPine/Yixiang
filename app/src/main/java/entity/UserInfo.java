package entity;

import java.util.Date;

public class UserInfo {

	// Fields

		private String userId;
		private String userPhone;
		private String userPwd;
		private String userName;
		private String icon;
		private Date birth;
		private Boolean sex;
		private String location;

		// Constructors

		/** default constructor */
		public UserInfo() {
		}

		/** minimal constructor */
		public UserInfo(String userPhone, String userPwd, String userName,
				Boolean sex) {
			this.userPhone = userPhone;
			this.userPwd = userPwd;
			this.userName = userName;
			this.sex = sex;
		}

		/** full constructor */
		public UserInfo(String userPhone, String userPwd, String userName,
				String icon, Date birth, Boolean sex, String location) {
			this.userPhone = userPhone;
			this.userPwd = userPwd;
			this.userName = userName;
			this.icon = icon;
			this.birth = birth;
			this.sex = sex;
			this.location = location;
		}

		// Property accessors

		public String getUserId() {
			return this.userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getUserPhone() {
			return this.userPhone;
		}

		public void setUserPhone(String userPhone) {
			this.userPhone = userPhone;
		}

		public String getUserPwd() {
			return this.userPwd;
		}

		public void setUserPwd(String userPwd) {
			this.userPwd = userPwd;
		}

		public String getUserName() {
			return this.userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getIcon() {
			return this.icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}

		public Date getBirth() {
			return this.birth;
		}

		public void setBirth(Date birth) {
			this.birth = birth;
		}

		public Boolean getSex() {
			return this.sex;
		}

		public void setSex(Boolean sex) {
			this.sex = sex;
		}

		public String getLocation() {
			return this.location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

}

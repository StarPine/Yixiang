package entity;

import java.util.Date;

public class VersionRecord {


	private String versionId;
	private String appType;
	private int versionNum;
	private Date updateDate;
	private String versionPath;
	public String getVersionId() {
		return versionId;
	}
	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}
	public String getAppType() {
		return appType;
	}
	public void setAppType(String appType) {
		this.appType = appType;
	}
	public int getVersionNum() {
		return versionNum;
	}
	public void setVersionNum(int versionNum) {
		this.versionNum = versionNum;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getVersionPath() {
		return versionPath;
	}
	public void setVersionPath(String versionPath) {
		this.versionPath = versionPath;
	}


}

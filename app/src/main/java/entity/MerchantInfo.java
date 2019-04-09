package entity;

public class MerchantInfo {
	
	// Fields

		private Integer merchantId;
		private String name;
		private String image;
		private String link;
		private Boolean activation;

		// Constructors

	/** default constructor */
	public MerchantInfo() {
	}

	/** minimal constructor */
	public MerchantInfo(String name, Boolean activation) {
		this.name = name;
		this.activation = activation;
	}

	/** full constructor */
	public MerchantInfo(String name, String image, String link,
			Boolean activation) {
		this.name = name;
		this.image = image;
		this.link = link;
		this.activation = activation;
	}

	// Property accessors

	public Integer getMerchantId() {
		return this.merchantId;
	}

	public void setMerchantId(Integer merchantId) {
		this.merchantId = merchantId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Boolean getActivation() {
		return this.activation;
	}

	public void setActivation(Boolean activation) {
		this.activation = activation;
	}

	
}

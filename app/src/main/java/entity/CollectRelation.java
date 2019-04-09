package entity;

import java.util.Date;

public class CollectRelation {

	// Fields

		private String collectId;
		private String userId;
		private String collectedUserId;
		private Date collectDate;

		// Constructors

		/** default constructor */
		public CollectRelation() {
		}

		/** full constructor */
		public CollectRelation(String userId, String collectedUserId,
				Date collectDate) {
			this.userId = userId;
			this.collectedUserId = collectedUserId;
			this.collectDate = collectDate;
		}

		// Property accessors

		public String getCollectId() {
			return this.collectId;
		}

		public void setCollectId(String collectId) {
			this.collectId = collectId;
		}

		public String getUserId() {
			return this.userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getCollectedUserId() {
			return this.collectedUserId;
		}

		public void setCollectedUserId(String collectedUserId) {
			this.collectedUserId = collectedUserId;
		}

		public Date getCollectDate() {
			return this.collectDate;
		}

		public void setCollectDate(Date collectDate) {
			this.collectDate = collectDate;
		}

}

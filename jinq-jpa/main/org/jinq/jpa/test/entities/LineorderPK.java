package org.jinq.jpa.test.entities;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the LINEORDERS database table.
 * 
 */
@Embeddable
public class LineorderPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private int saleid;
	private int itemid;

	public LineorderPK() {
	}

	public int getSaleid() {
		return this.saleid;
	}
	public void setSaleid(int saleid) {
		this.saleid = saleid;
	}

	public int getItemid() {
		return this.itemid;
	}
	public void setItemid(int itemid) {
		this.itemid = itemid;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof LineorderPK)) {
			return false;
		}
		LineorderPK castOther = (LineorderPK)other;
		return 
			(this.saleid == castOther.saleid)
			&& (this.itemid == castOther.itemid);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.saleid;
		hash = hash * prime + this.itemid;
		
		return hash;
	}
}
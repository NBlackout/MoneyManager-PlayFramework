package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Transaction extends Model {

	private static final long serialVersionUID = 2305214514018528308L;

	@ManyToOne
	public Account account;

	public String label;

	public double amount;

	public long date;
}

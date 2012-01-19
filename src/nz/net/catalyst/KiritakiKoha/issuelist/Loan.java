package nz.net.catalyst.KiritakiKoha.issuelist;
/*
 * Holder Class Referenced by IssueListActivity.
 * Represents Data from a book out on loan by the user.
 */


public class Loan extends Object
{
	private String name = "Random_Name";
	private int index = -1;
	private boolean overdue = false;
	private String dueDate = "";
	private String author = "";
	public Loan(int i, String name)
	{
		this.name = name;
		this.index = i;
	}
	
	public void setAuthor(String s)
	{
		author = s;
	}
	
	public String getAuthor()
	{
		return author;
	}
	
	public void setDueDate(String s)
	{
		dueDate = s;
	}
	
	public String getDueDate()
	{
		return dueDate;
	}
	
	public boolean isOverdue()
	{
		return overdue;
	}
	
	public String getGroup()
	{
		return (index+1)+". "+name;
	}
	
	public void setOverdue(boolean b)
	{
		this.overdue = b;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDesc()
	{
		return "Author: "+getAuthor()+"<br>Due: "+getDueDate();
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}

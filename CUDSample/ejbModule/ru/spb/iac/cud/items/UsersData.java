package ru.spb.iac.cud.items;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "usersData", propOrder = {
    "count",
    "users"
})
public class UsersData {
	
	@XmlElement(name = "count"/*,*/ /*required=true,*/ /*namespace = "http://util.services.cud.iac.spb.ru/"*/)
	private Integer count;
	
	@XmlElement(name = "users"/*,*/ /*required=true,*/ /*namespace = "http://util.services.cud.iac.spb.ru/"*/)
	private List<User> users = new ArrayList<User>() ;
  //  private List<String> roles;

    public UsersData() {
    }

    public Integer getCount() {
        return this.count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

    
    
 /*
    @Override
    public String toString() {
        return "{user " + userAttributes;
    }*/
}

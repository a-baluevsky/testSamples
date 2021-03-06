package ru.spb.iac.cud.items;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "groupsData", propOrder = {
    "count",
    "groups"
})
public class GroupsData {
	
	@XmlElement(name = "count"/*,*/ /*required=true,*/ /*namespace = "http://util.services.cud.iac.spb.ru/"*/)
	private Integer count;
	
	@XmlElement(name = "groups"/*,*/ /*required=true,*/ /*namespace = "http://util.services.cud.iac.spb.ru/"*/)
	private List<Group> groups = new ArrayList<Group>() ;
  //  private List<String> roles;

    public GroupsData() {
    }

    public Integer getCount() {
        return this.count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	
 
 /*
    @Override
    public String toString() {
        return "{user " + userAttributes;
    }*/
}

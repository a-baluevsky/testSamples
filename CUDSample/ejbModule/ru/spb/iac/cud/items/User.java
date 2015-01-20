package ru.spb.iac.cud.items;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "users", propOrder = {
	"uid",
	"attributes",
    "codesRoles",
    "codesGroups"
})
public class User{
	
	@XmlElement(name = "uid" /*required=true,*/ /*namespace = "http://util.services.cud.iac.spb.ru/"*/)
	private String uid;
	
	@XmlElement(name = "attributes" /*required=true,*/ /*namespace = "http://util.services.cud.iac.spb.ru/"*/)
	private List<Attribute> attributes = new ArrayList<Attribute>() ;
 
	@XmlElement(name = "codesRoles" /*required=true,*/ /*namespace = "http://util.services.cud.iac.spb.ru/"*/)
	private List<String> codesRoles;

	@XmlElement(name = "codesGroups" /*required=true,*/ /*namespace = "http://util.services.cud.iac.spb.ru/"*/)
	private List<String> codesGroups;
	
    public User() {
    }

    public String getUid() {
        return this.uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
    
	public List<String> getCodesRoles() {
		return codesRoles;
	}

	public void setCodesRoles(List<String> codesRoles) {
		this.codesRoles = codesRoles;
	}

	public List<String> getCodesGroups() {
		return codesGroups;
	}

	public void setCodesGroups(List<String> codesGroups) {
		this.codesGroups = codesGroups;
	}

  
}

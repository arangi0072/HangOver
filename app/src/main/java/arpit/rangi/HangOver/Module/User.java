package arpit.rangi.HangOver.Module;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String username;
    private String name;
    private String about;
    private String account;
    private String uid;
    private String image;
    private String token;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public User() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User(String username, String name, String about, String image, String uid, String token) {
        this.username = username;
        this.uid = uid;
        this.name = name;
        this.about = about;
        this.token = token;
        this.image = image;
    }
    public User(String username, String name, String about, String account, String image, String uid, String token) {
        this.username = username;
        this.uid = uid;
        this.token = token;
        this.name = name;
        this.about = about;
        this.account = account;
        this.image = image;
    }

    public User(String username, String name, String about, String image, String uid) {
        this.username = username;
        this.name = name;
        this.about = about;
        this.uid = uid;
        this.image = image;
    }
    public User(String name, String about, String image) {
        this.name = name;
        this.about = about;
        this.image = image;
    }




    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public Map<String, Object> toMap(){
        Map<String, Object> data = new HashMap<>();
        data.put("username", this.username);
        data.put("name", this.name);
        data.put("about", this.about);
        data.put("token", this.token);
        data.put("account", this.account);
        data.put("uid", this.uid);
        data.put("image", this.image);
        return data;
    }
}

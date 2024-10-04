package utils;

/**
 *
 * @author ByteBigBoss
 * @org ImaginecoreX
 */
public class Validator {

    public static boolean VALIDATE_EMAIL(String email){
     return email.matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    }
    
    public static boolean VALIDATE_MOBILE(String mobile){
        return mobile.matches("^07[01245678]{1}[0-9]{7}$");
    }
    
    public static boolean VALIDATE_PASSWORD (String password){
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$");
    }
    
    public static boolean VALIDATE_DOUBLE (String text){
        return text.matches("^\\d+(\\.\\d{2})?$");
    }
    
    public static boolean VALIDATE_INTEGER(String text){
        return text.matches("^\\d+$");
    }
}

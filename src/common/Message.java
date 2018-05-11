package common;

import java.io.Serializable;

public class Message implements Serializable {
    //Integers of all possible message types
    public static final int POSTCODE = 0,
                            POSTCODE_LIST = 1,
                            DISH = 2,
                            DISH_LIST = 3;

    private int type;
    private Object payload;

    public Message(int type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public int getType() {
        return this.type;
    }

    public Object getPayload() {
        return this.payload;
    }
}

package comms;

import java.io.Serializable;

/**
 * Enum to store all the possible message types
 * @author Oscar van Leusen
 */
public enum MessageType implements Serializable {
    REGISTER, REGISTER_SUCCESS,
    LOGIN, LOGIN_SUCCESS,
    GET_POSTCODES, POSTCODES,
    GET_DISHES, DISHES,
    GET_DISH_DESC, DISH_DESC,
    GET_DISH_PRICE, DISH_PRICE,
    GET_BASKET, BASKET,
    GET_BASKET_COST, BASKET_COST,
    GET_ORDERS, ORDERS,
    GET_STATUS, STATUS,
    GET_COST, COST,
    ADD_DISH,
    UPDATE_DISH,
    SEND_CHECKOUT, ORDER,
    SEND_CLEAR,
    SEND_CANCEL,
    UPDATE
}
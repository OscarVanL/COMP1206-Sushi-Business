package common;

/**
 * @author Oscar van Leusen
 */
public enum MessageType {
    REGISTER, LOGIN, USER,
    GET_POSTCODES, POSTCODES,
    GET_DISHES, DISHES,
    GET_DISH_DESC, DISH_DESC,
    GET_DISH_PRICE, DISH_PRICE,
    GET_BASKET, BASKET,
    GET_BASKET_COST, BASKET_COST,
    GET_ORDERS, ORDERS,
    GET_STATUS, STATUS,
    GET_COST, COST,
    //Messages sent from Client to sever
    SEND_DISH,
    SEND_CHECKOUT, ORDER,
    SEND_CLEAR,
    SEND_CANCEL,
}
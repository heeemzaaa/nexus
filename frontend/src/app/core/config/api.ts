// const URL = "http://localhost:8080"
const URL = ""
export const API = {
    ME: `${URL}/api/users/me`,
    REGISTER: `${URL}/api/auth/register`,
    LOGIN: `${URL}/api/auth/login`,
    GET_POSTS: `${URL}/api/products`,
    PROFILE: `${URL}/api/users`,
    GET_PROFILE_POSTS: `${URL}/api/products/users`,
    CREATE_PRODUCT: `${URL}/api/products`,
    CREATE_MEDIA: `${URL}/api/media`,
    DELETE_PRODUCT: `${URL}/api/products`,
    UPDATE_PRODUCT: `${URL}/api/products`,
    CREATE_ITEM: `${URL}/api/carts`,
    GET_ITEMS: `${URL}/api/carts`,
    UPDATE_ITEM: `${URL}/api/carts`,
    DELETE_ITEM: `${URL}/api/carts`,
    CREATE_ORDER: `${URL}/api/orders`,
    GET_ORDERS: `${URL}/api/orders`,
}

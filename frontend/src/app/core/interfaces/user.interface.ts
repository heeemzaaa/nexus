export enum userRole {
    CLIENT,
    SELLER
}

export interface User {
    id: string,
    name: string,
    email: string,
    role: userRole
    avatar: string,
    token: string
}
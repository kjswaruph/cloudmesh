import { gql } from '@apollo/client';

export const GET_USERS = gql/* GraphQL */`
    query GetUsers {
        users {
            userId
            firstName
            lastName
            email
            username
            dateCreated
        }
    }
`;

export const GET_USER = gql/* GraphQL */`
    query GetUser($id: ID!) {
        user(id: $id) {
            userId
            firstName
            lastName
            email
            username
            dateCreated
        }
    }
`;
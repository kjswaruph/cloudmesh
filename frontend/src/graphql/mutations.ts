import { gql } from '@apollo/client';

export const ADD_USER = gql/* GraphQL */`
	mutation AddUser(
		$firstName: String!
		$lastName: String!
		$username: String!
		$email: String!
		$password: String!
	) {
		addUser(
			firstName: $firstName
			lastName: $lastName
			username: $username
			email: $email
			password: $password
		) {
			id
			firstName
			lastName
			username
			email
			dateCreated
			dateUpdated
		}
	}
`;

export interface AddUserVariables {
	firstName: string;
	lastName: string;
	username: string;
	email: string;
	password: string;
}

export const DELETE_USER = gql/* GraphQL */`
	mutation DeleteUser($id: ID!) {
		deleteUser(id: $id)
	}
`;



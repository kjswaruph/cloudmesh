import { ApolloClient, InMemoryCache, HttpLink } from '@apollo/client';

const httpLink = new HttpLink({
    uri: process.env.NEXT_PUBLIC_GRAPHQL_ENDPOINT || 'http://localhost:8080/graphql',
    credentials: 'include',
});

const client = new ApolloClient({
    // Enable SSR optimizations when rendering on server in Next.js
    ssrMode: typeof window === 'undefined',
    link: httpLink,
    cache: new InMemoryCache({
        // Example type policies (extend as needed when schema grows)
        typePolicies: {
            User: { keyFields: ['id'] },
            Query: {
                fields: {
                    users: {
                        merge(_existing, incoming) { return incoming; },
                    },
                },
            },
        },
    }),
});

export default client;

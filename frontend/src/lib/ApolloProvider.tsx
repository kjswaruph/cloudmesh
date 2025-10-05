"use client";
import { ReactNode } from 'react';
import { ApolloProvider } from '@apollo/client/react'; // Using explicit react entry to satisfy TS exports
import client from './apolloClient';

export function GraphQLProvider({ children }: { children: ReactNode }) {
  return <ApolloProvider client={client}>{children}</ApolloProvider>;
}

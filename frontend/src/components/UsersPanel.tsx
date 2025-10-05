"use client";
import { useState } from 'react';
// NOTE: Apollo React hooks import issues in current environment; using manual fetch against GraphQL endpoint instead.
// This component strictly uses backend-supported operations: users, addUser, deleteUser.
import { AddUserVariables } from '@/graphql/mutations';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import { Trash2 } from 'lucide-react';

interface User {
  id: string;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  dateCreated: string;
  dateUpdated: string;
}

export function UsersPanel() {
  const [data, setData] = useState<{ users: User[] } | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [adding, setAdding] = useState<boolean>(false);

  const graphql = async <T,>(query: string, variables?: Record<string, any>): Promise<T> => {
    const res = await fetch('http://localhost:8080/graphql', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query, variables }),
    });
    const json = await res.json();
    if (json.errors?.length) throw new Error(json.errors[0].message);
    return json.data as T;
  };

  const loadUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await graphql<{ users: User[] }>(`query GetUsers { users { id firstName lastName username email dateCreated dateUpdated } }`);
      setData(result);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  // Initial load
  if (!data && !loading && !error) {
    loadUsers();
  }

  const [form, setForm] = useState<AddUserVariables>({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.firstName || !form.lastName || !form.username || !form.email || !form.password) return;
    try {
      setAdding(true);
      await graphql<{ addUser: User }>(
        `mutation AddUser($firstName: String!, $lastName: String!, $username: String!, $email: String!, $password: String!) {
          addUser(firstName: $firstName, lastName: $lastName, username: $username, email: $email, password: $password) {
            id
          }
        }`,
        form
      );
      await loadUsers();
    } finally {
      setAdding(false);
    }
    setForm({ firstName: '', lastName: '', username: '', email: '', password: '' });
  };

  const remove = async (id: string) => {
    // Simple confirmation until UX spec refined.
    if (!confirm('Delete user?')) return;
    await graphql<{ deleteUser: boolean }>(`mutation DeleteUser($id: ID!) { deleteUser(id: $id) }`, { id });
    await loadUsers();
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Users</CardTitle>
        <CardDescription>Managed via GraphQL (addUser, deleteUser, users)</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <form onSubmit={submit} className="grid gap-2 md:grid-cols-6">
          <Input name="firstName" placeholder="First" value={form.firstName} onChange={handleChange} className="md:col-span-1" required />
          <Input name="lastName" placeholder="Last" value={form.lastName} onChange={handleChange} className="md:col-span-1" required />
          <Input name="username" placeholder="Username" value={form.username} onChange={handleChange} className="md:col-span-1" required />
          <Input name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} className="md:col-span-1" required />
          <Input name="password" type="password" placeholder="Password" value={form.password} onChange={handleChange} className="md:col-span-1" required />
          <Button type="submit" disabled={adding} className="md:col-span-1">{adding ? 'Adding...' : 'Add User'}</Button>
        </form>

        {loading && (
          <div className="space-y-2">
            {Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-6 w-full" />)}
          </div>
        )}
        {error && (
          <div className="text-sm text-destructive">Error loading users: {error}</div>
        )}
        {!loading && !error && (
          <ul className="divide-y border rounded-md">
            {data?.users.map((u: User) => (
              <li key={u.id} className="flex items-center justify-between p-2 text-sm">
                <div>
                  <span className="font-medium">{u.username}</span> <span className="text-muted-foreground">({u.email})</span>
                  <span className="ml-2 text-xs text-muted-foreground">{u.firstName} {u.lastName}</span>
                </div>
                <Button size="icon" variant="ghost" onClick={() => remove(u.id)} aria-label={`Delete ${u.username}`}>
                  <Trash2 className="h-4 w-4" />
                </Button>
              </li>
            ))}
            {(!data || data.users.length === 0) && (
              <li className="p-2 text-muted-foreground text-sm">No users found.</li>
            )}
          </ul>
        )}

        {/* TODO(feature): Role management, password reset, pagination, search, etc. require additional backend GraphQL support. */}
      </CardContent>
    </Card>
  );
}

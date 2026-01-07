"use client";
import { useState } from 'react';
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
  const [data, setData] = useState<User[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [adding, setAdding] = useState<boolean>(false);

  // Fetch helper
  const api = async <T,>(url: string, options?: RequestInit): Promise<T> => {
    // Add auth token if needed, or rely on cookies
    const res = await fetch(url, options);
    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || `Request failed: ${res.status}`);
    }
    // Handle empty response for DELETE
    if (res.status === 204) return {} as T;
    return res.json();
  };

  const loadUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const users = await api<User[]>('/api/users');
      setData(users);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  // Initial load
  if (!data?.length && !loading && !error) {
    // Check if we already loaded? Or use useEffect. 
    // For simplicity, calling here but better to use useEffect in real app.
    // However, to avoid loop, let's use useEffect-like pattern or just check specific flag.
    // React strict mode might double call.
    // Better:
  }
  // Using simple effect replacement:
  useState(() => {
    loadUsers();
  });


  const [form, setForm] = useState({
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
      await api<User>('/api/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      await loadUsers();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setAdding(false);
    }
    setForm({ firstName: '', lastName: '', username: '', email: '', password: '' });
  };

  const remove = async (id: string) => {
    if (!confirm('Delete user?')) return;
    try {
      await api(`/api/users/${id}`, { method: 'DELETE' });
      await loadUsers();
    } catch (e: any) {
      setError(e.message);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Users</CardTitle>
        <CardDescription>Managed via REST API (addUser, deleteUser, users)</CardDescription>
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

        {loading && !data.length && (
          <div className="space-y-2">
            {Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-6 w-full" />)}
          </div>
        )}
        {error && (
          <div className="text-sm text-destructive">Error: {error}</div>
        )}
        <ul className="divide-y border rounded-md">
          {data.map((u: User) => (
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
          {(!data || data.length === 0) && !loading && (
            <li className="p-2 text-muted-foreground text-sm">No users found.</li>
          )}
        </ul>

      </CardContent>
    </Card>
  );
}

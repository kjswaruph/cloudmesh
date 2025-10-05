"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { DashboardLayout } from "@/components/DashboardLayout";
import { DashboardOverview } from "@/components/DashboardOverview";
import { DashboardCharts } from "@/components/DashboardCharts";
import { ResourceTable } from "@/components/ResourceTable";
import { UsersPanel } from "../../components/UsersPanel";

export default function DashboardPage() {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const res = await fetch('/auth/me', {
          credentials: 'include',
          cache: 'no-store'
        });

        if (res.ok) {
          const data = await res.json();
          if (data.username) {
            setIsAuthenticated(true);
          } else {
            router.replace('/login?redirect=/dashboard');
          }
        } else {
          router.replace('/login?redirect=/dashboard');
        }
      } catch (error) {
        console.error('Auth check failed:', error);
        router.replace('/login?redirect=/dashboard');
      } finally {
        setIsLoading(false);
      }
    };

    checkAuth();
  }, [router]);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
          <p className="mt-4 text-muted-foreground">Loading...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <DashboardLayout>
      <div className="space-y-8">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Dashboard</h2>
          <p className="text-muted-foreground">
            Welcome to your multi-cloud management center
          </p>
        </div>
        
  {/* Overview Cards (currently mock data until backend provides GraphQL) */}
        <DashboardOverview />
        
        {/* Charts Section (mock data + TODO placeholder) */}
        <div className="space-y-4">
          <h3 className="text-xl font-semibold">Analytics</h3>
          <DashboardCharts />
        </div>
        
        {/* Resources Table (mock data + TODO placeholder) */}
        <div className="space-y-4">
          <h3 className="text-xl font-semibold">Resource Management</h3>
          <ResourceTable />
        </div>

        {/* Users managed purely via existing GraphQL operations */}
        <div className="space-y-4">
          <h3 className="text-xl font-semibold">Users</h3>
          <UsersPanel />
        </div>
      </div>
    </DashboardLayout>
  );
}
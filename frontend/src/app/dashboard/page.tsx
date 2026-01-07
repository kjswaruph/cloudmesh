"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { DashboardLayout } from "@/components/DashboardLayout";
import { DashboardOverview } from "@/components/DashboardOverview";
import api from "@/lib/api-client";

export default function DashboardPage() {
  const router = useRouter();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const data = await api.auth.me();
        if (data && (data.username || data.user)) {
          setIsAuthenticated(true);
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
        
        <DashboardOverview />
      </div>
    </DashboardLayout>
  );
}
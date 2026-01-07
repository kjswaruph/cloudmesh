"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { DashboardLayout } from "@/components/DashboardLayout";
import { ResourcesPanel } from "@/components/ResourcesPanel";
import api from "@/lib/api-client";

export default function ResourcesPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        await api.auth.me();
        setLoading(false);
      } catch (error) {
        router.push("/login");
      }
    };

    checkAuth();
  }, [router]);

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-muted-foreground">Loading...</div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Cloud Resources</h1>
          <p className="text-muted-foreground mt-2">
            Manage your infrastructure across all cloud providers
          </p>
        </div>
        <ResourcesPanel />
      </div>
    </DashboardLayout>
  );
}

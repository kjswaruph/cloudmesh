"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Progress } from "@/components/ui/progress";
import { Badge } from "@/components/ui/badge";
import { 
  Cloud, 
  DollarSign, 
  Server, 
  Shield, 
  TrendingUp, 
  TrendingDown,
  AlertTriangle 
} from "lucide-react";

interface CloudProvider {
  name: string;
  resources: number;
  cost: number;
  status: "healthy" | "warning" | "error";
  usage: number;
}

interface DashboardStats {
  totalResources: number;
  totalCost: number;
  activeProviders: number;
  securityScore: number;
  providers: CloudProvider[];
}

export function DashboardOverview() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardStats = async () => {
      // TODO(graphql): Await backend implementation of a 'dashboardStats' GraphQL query.
      // This component currently uses mock data (see catch) to visualize layout.
      try {
        // TODO: Replace with actual backend API endpoint
        const response = await fetch('/api/dashboard/overview', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`, // TODO: Use proper auth context
          },
        });

        if (response.status === 401) {
          // TODO: Redirect to login or show unauthorized message
          setError('Unauthorized access. Please log in.');
          return;
        }

        if (!response.ok) {
          throw new Error('Failed to fetch dashboard data');
        }

        const data = await response.json();
        setStats(data);
      } catch (err) {
        console.error('Error fetching dashboard stats:', err);
        // TODO: Mock data for development - remove in production
        setStats({
          totalResources: 847,
          totalCost: 4250.80,
          activeProviders: 3,
          securityScore: 92,
          providers: [
            { name: "AWS", resources: 342, cost: 1890.50, status: "healthy", usage: 75 },
            { name: "Azure", resources: 298, cost: 1650.30, status: "warning", usage: 82 },
            { name: "GCP", resources: 207, cost: 710.00, status: "healthy", usage: 45 },
          ],
        });
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardStats();
  }, []);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <Card key={i}>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-4 w-4" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-8 w-16 mb-2" />
                <Skeleton className="h-3 w-32" />
              </CardContent>
            </Card>
          ))}
        </div>
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Card key={i}>
              <CardHeader>
                <Skeleton className="h-6 w-24" />
              </CardHeader>
              <CardContent className="space-y-3">
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-3/4" />
                <Skeleton className="h-4 w-1/2" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <Card className="border-destructive/50">
        <CardHeader>
          <CardTitle className="text-destructive">Error Loading Dashboard</CardTitle>
          <CardDescription>{error}</CardDescription>
        </CardHeader>
      </Card>
    );
  }

  if (!stats) return null;

  const getStatusColor = (status: string) => {
    switch (status) {
      case "healthy": return "bg-green-500";
      case "warning": return "bg-yellow-500";
      case "error": return "bg-red-500";
      default: return "bg-gray-500";
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "healthy": return <Badge className="bg-green-100 text-green-800">Healthy</Badge>;
      case "warning": return <Badge className="bg-yellow-100 text-yellow-800">Warning</Badge>;
      case "error": return <Badge className="bg-red-100 text-red-800">Error</Badge>;
      default: return <Badge>Unknown</Badge>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Overview Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Resources</CardTitle>
            <Server className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalResources.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">
              <TrendingUp className="inline h-3 w-3 mr-1" />
              +12% from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Monthly Cost</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${stats.totalCost.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">
              <TrendingDown className="inline h-3 w-3 mr-1" />
              -5% from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Providers</CardTitle>
            <Cloud className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.activeProviders}</div>
            <p className="text-xs text-muted-foreground">AWS, Azure, GCP</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Security Score</CardTitle>
            <Shield className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.securityScore}%</div>
            <p className="text-xs text-muted-foreground">
              {stats.securityScore >= 90 ? (
                <>
                  <TrendingUp className="inline h-3 w-3 mr-1" />
                  Excellent security
                </>
              ) : (
                <>
                  <AlertTriangle className="inline h-3 w-3 mr-1" />
                  Needs attention
                </>
              )}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Provider Details */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {stats.providers.map((provider) => (
          <Card key={provider.name}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-lg font-medium">{provider.name}</CardTitle>
              {getStatusBadge(provider.status)}
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span>Resources</span>
                  <span className="font-medium">{provider.resources}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Monthly Cost</span>
                  <span className="font-medium">${provider.cost.toLocaleString()}</span>
                </div>
              </div>
              
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span>Usage</span>
                  <span className="font-medium">{provider.usage}%</span>
                </div>
                <Progress value={provider.usage} className="h-2" />
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
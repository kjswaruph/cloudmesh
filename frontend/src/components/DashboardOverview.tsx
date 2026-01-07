"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api-client";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { 
  Cloud, 
  DollarSign, 
  Server, 
  Shield, 
  TrendingUp, 
  TrendingDown,
  AlertCircle,
  Activity,
  ExternalLink
} from "lucide-react";
import Image from "next/image";
import Link from "next/link";

interface CloudProvider {
  name: string;
  credentialId: string;
  resources: number;
  cost: number;
  status: "healthy" | "warning" | "error";
  region?: string;
}

interface DashboardStats {
  totalResources: number;
  totalCost: number;
  activeProviders: number;
  activeProjects: number;
  providers: CloudProvider[];
}

const providerIcons: Record<string, string> = {
  AWS: "/providers/aws.png",
  GCP: "/providers/gcp.png",
  Azure: "/providers/azure.png",
  DigitalOcean: "/providers/do.png"
};

export function DashboardOverview() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardStats = async () => {
      try {
        const [credentials, projects] = await Promise.all([
          api.credentials.list().catch(() => []),
          api.projects.list().catch(() => [])
        ]);

        let totalCost = 0;
        const providerData: CloudProvider[] = [];

        const endDate = new Date().toISOString().split('T')[0];
        const startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

        await Promise.all(
          credentials.map(async (cred: any) => {
            try {
              const costs = await api.costs.getCredentialCosts(cred.id, startDate, endDate);
              const credentialCost = costs.reduce((sum: number, c: any) => sum + (c.totalCost || 0), 0);
              
              totalCost += credentialCost;
              
              providerData.push({
                name: cred.provider || 'Unknown',
                credentialId: cred.id,
                resources: cred.resourceCount || 0,
                cost: credentialCost,
                status: credentialCost > 1000 ? "warning" : "healthy",
                region: cred.region
              });
            } catch (err) {
              // If costs API fails, still show the provider without cost data
              providerData.push({
                name: cred.provider || 'Unknown',
                credentialId: cred.id,
                resources: cred.resourceCount || 0,
                cost: 0,
                status: "healthy",
                region: cred.region
              });
              console.warn(`Failed to fetch costs for ${cred.id}:`, err);
            }
          })
        );

        let totalResources = 0;
        await Promise.all(
          projects.map(async (project: any) => {
            try {
              const resources = await api.projects.getResources(project.id);
              totalResources += resources.length;
            } catch (err) {
              console.warn(`Failed to fetch resources for project ${project.id}:`, err);
            }
          })
        );

        setStats({
          totalResources,
          totalCost,
          activeProviders: credentials.length,
          activeProjects: projects.length,
          providers: providerData
        });
      } catch (err) {
        console.error('Error fetching dashboard stats:', err);
        // Set default stats instead of showing error
        setStats({
          totalResources: 0,
          totalCost: 0,
          activeProviders: 0,
          activeProjects: 0,
          providers: []
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
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    );
  }

  if (!stats) return null;

  const getStatusColor = (status: string) => {
    switch (status) {
      case "healthy": return "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300";
      case "warning": return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300";
      case "error": return "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300";
      default: return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
    }
  };

  return (
    <div className="space-y-6">
      {/* Overview Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Resources</CardTitle>
            <Server className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalResources}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Across all providers
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Monthly Spending</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${stats.totalCost.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Last 30 days
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Connected Providers</CardTitle>
            <Cloud className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.activeProviders}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Cloud accounts
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Projects</CardTitle>
            <Activity className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.activeProjects}</div>
            <p className="text-xs text-muted-foreground mt-1">
              In your workspace
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Connected Providers */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Connected Providers</CardTitle>
              <CardDescription>
                Overview of your cloud provider accounts
              </CardDescription>
            </div>
            <Link href="/dashboard/providers">
              <Button variant="outline" size="sm">
                Manage
                <ExternalLink className="ml-2 h-4 w-4" />
              </Button>
            </Link>
          </div>
        </CardHeader>
        <CardContent>
          {stats.providers.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-8 text-center">
              <Cloud className="h-12 w-12 text-muted-foreground mb-4" />
              <p className="text-sm text-muted-foreground">No cloud providers connected</p>
              <Link href="/dashboard/providers">
                <Button variant="outline" size="sm" className="mt-4">
                  Connect Provider
                </Button>
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {stats.providers.map((provider) => (
                <div
                  key={provider.credentialId}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent/50 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <Image
                      src={providerIcons[provider.name] || providerIcons.AWS}
                      alt={provider.name}
                      width={40}
                      height={40}
                      className="object-contain"
                    />
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-medium">{provider.name}</span>
                        <Badge className={getStatusColor(provider.status)}>
                          {provider.status}
                        </Badge>
                      </div>
                      {provider.region && (
                        <p className="text-sm text-muted-foreground">
                          Region: {provider.region}
                        </p>
                      )}
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="flex items-center gap-6">
                      <div>
                        <p className="text-sm text-muted-foreground">Resources</p>
                        <p className="text-lg font-semibold">{provider.resources}</p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">Monthly Cost</p>
                        <p className="text-lg font-semibold">${provider.cost.toFixed(2)}</p>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card className="hover:border-primary transition-colors cursor-pointer">
          <Link href="/dashboard/resources">
            <CardHeader>
              <CardTitle className="text-base flex items-center gap-2">
                <Server className="h-4 w-4" />
                Manage Resources
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground">
                View and control your cloud infrastructure
              </p>
            </CardContent>
          </Link>
        </Card>

        <Card className="hover:border-primary transition-colors cursor-pointer">
          <Link href="/dashboard/billing">
            <CardHeader>
              <CardTitle className="text-base flex items-center gap-2">
                <DollarSign className="h-4 w-4" />
                View Billing
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground">
                Monitor costs and manage invoices
              </p>
            </CardContent>
          </Link>
        </Card>

        <Card className="hover:border-primary transition-colors cursor-pointer">
          <Link href="/projects">
            <CardHeader>
              <CardTitle className="text-base flex items-center gap-2">
                <Activity className="h-4 w-4" />
                Manage Projects
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground">
                Organize your cloud resources by project
              </p>
            </CardContent>
          </Link>
        </Card>
      </div>
    </div>
  );
}
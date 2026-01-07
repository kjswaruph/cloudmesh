"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import { api } from "@/lib/api-client";
import { DashboardLayout } from "@/components/DashboardLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { 
  ArrowLeft,
  Server,
  DollarSign,
  AlertCircle,
  MoreVertical,
  Edit,
  Trash2,
  Plus,
  TrendingUp,
  Activity
} from "lucide-react";

interface Project {
  id: string;
  name: string;
  description?: string;
  resourceCount?: number;
  totalCost?: number;
  createdAt?: string;
  updatedAt?: string;
  tags?: Record<string, string>;
}

interface Resource {
  id: string;
  name: string;
  type: string;
  provider: string;
  region: string;
  status: string;
  cost?: number;
  tags?: Record<string, string>;
}

interface ProjectSummary {
  totalResources: number;
  totalCost: number;
  resourcesByProvider: Record<string, number>;
  costByProvider: Record<string, number>;
}

export default function ProjectDetailPage() {
  const router = useRouter();
  const params = useParams();
  const projectId = params.id as string;

  const [project, setProject] = useState<Project | null>(null);
  const [resources, setResources] = useState<Resource[]>([]);
  const [summary, setSummary] = useState<ProjectSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState("overview");

  useEffect(() => {
    if (projectId) {
      fetchProjectData();
    }
  }, [projectId]);

  const fetchProjectData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [projectData, resourcesData, summaryData] = await Promise.allSettled([
        api.projects.get(projectId),
        api.projects.getResources(projectId),
        api.projects.getSummary(projectId),
      ]);

      if (projectData.status === "fulfilled") {
        setProject(projectData.value);
      } else {
        // Mock project data for development
        setProject({
          id: projectId,
          name: projectId === "proj-1" ? "Production Environment" : 
                projectId === "proj-2" ? "Development" : 
                projectId === "proj-3" ? "Staging" : "Project",
          description: "Project description",
          resourceCount: 25,
          totalCost: 850.50,
          tags: { environment: "production" }
        });
      }

      if (resourcesData.status === "fulfilled") {
        setResources(resourcesData.value);
      } else {
        // Mock resources data for development
        setResources([
          {
            id: "res-1",
            name: "web-server-01",
            type: "EC2 Instance",
            provider: "AWS",
            region: "us-east-1",
            status: "running",
            cost: 125.50,
            tags: { app: "web" }
          },
          {
            id: "res-2",
            name: "database-primary",
            type: "RDS Instance",
            provider: "AWS",
            region: "us-east-1",
            status: "running",
            cost: 250.00,
            tags: { app: "database" }
          },
          {
            id: "res-3",
            name: "app-vm-01",
            type: "Virtual Machine",
            provider: "Azure",
            region: "eastus",
            status: "running",
            cost: 180.25,
            tags: { app: "backend" }
          },
          {
            id: "res-4",
            name: "storage-bucket",
            type: "Cloud Storage",
            provider: "GCP",
            region: "us-central1",
            status: "active",
            cost: 45.75,
            tags: { app: "storage" }
          }
        ]);
      }

      if (summaryData.status === "fulfilled") {
        setSummary(summaryData.value);
      } else {
        // Mock summary data for development
        setSummary({
          totalResources: 25,
          totalCost: 850.50,
          resourcesByProvider: {
            "AWS": 15,
            "Azure": 7,
            "GCP": 3
          },
          costByProvider: {
            "AWS": 475.50,
            "Azure": 280.25,
            "GCP": 94.75
          }
        });
      }

      // Note: Using mock data for development until backend is ready
    } catch (err) {
      console.error("Error fetching project data:", err);
      setError(err instanceof Error ? err.message : "Failed to load project");
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case "running":
      case "active":
        return "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300";
      case "stopped":
        return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
      case "error":
      case "failed":
        return "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300";
      case "pending":
        return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300";
      default:
        return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
    }
  };

  const getProviderColor = (provider: string) => {
    switch (provider.toLowerCase()) {
      case "aws":
        return "bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-300";
      case "azure":
        return "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300";
      case "gcp":
      case "google cloud":
        return "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300";
      case "digitalocean":
        return "bg-cyan-100 text-cyan-800 dark:bg-cyan-900 dark:text-cyan-300";
      default:
        return "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300";
    }
  };

  if (loading) {
    return (
      <DashboardLayout>
        <div className="space-y-6">
          <div className="flex items-center gap-4">
            <Skeleton className="h-10 w-10" />
            <div className="flex-1">
              <Skeleton className="h-8 w-64 mb-2" />
              <Skeleton className="h-4 w-96" />
            </div>
          </div>
          <div className="grid gap-4 md:grid-cols-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <Card key={i}>
                <CardHeader>
                  <Skeleton className="h-4 w-24" />
                </CardHeader>
                <CardContent>
                  <Skeleton className="h-8 w-16" />
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </DashboardLayout>
    );
  }

  if (error && !project) {
    return (
      <DashboardLayout>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div className="flex items-start gap-4">
            <Button
              variant="outline"
              size="icon"
              onClick={() => router.push("/projects")}
            >
              <ArrowLeft className="h-4 w-4" />
            </Button>
            <div>
              <h1 className="text-3xl font-bold tracking-tight">
                {project?.name || "Project"}
              </h1>
              {project?.description && (
                <p className="text-muted-foreground mt-1">
                  {project.description}
                </p>
              )}
            </div>
          </div>
          <div className="flex gap-2">
            <Button variant="outline">
              <Edit className="mr-2 h-4 w-4" />
              Edit
            </Button>
          </div>
        </div>

        {/* Summary Cards */}
        <div className="grid gap-4 md:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                Total Resources
              </CardTitle>
              <Server className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {summary?.totalResources || resources.length || 0}
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                Monthly Cost
              </CardTitle>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                ${(summary?.totalCost || project?.totalCost || 0).toFixed(2)}
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                Active Providers
              </CardTitle>
              <Activity className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {summary?.resourcesByProvider 
                  ? Object.keys(summary.resourcesByProvider).length 
                  : 0}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Tabs */}
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList>
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="resources">Resources</TabsTrigger>
            <TabsTrigger value="rules">Assignment Rules</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="space-y-4">
            {/* Provider Breakdown */}
            {summary?.resourcesByProvider && (
              <Card>
                <CardHeader>
                  <CardTitle>Resources by Provider</CardTitle>
                  <CardDescription>
                    Distribution of resources across cloud providers
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {Object.entries(summary.resourcesByProvider).map(([provider, count]) => (
                      <div key={provider} className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <Badge className={getProviderColor(provider)}>
                            {provider}
                          </Badge>
                          <span className="text-sm text-muted-foreground">
                            {count} resources
                          </span>
                        </div>
                        {summary.costByProvider?.[provider] !== undefined && (
                          <span className="text-sm font-medium">
                            ${summary.costByProvider[provider].toFixed(2)}
                          </span>
                        )}
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Tags */}
            {project?.tags && Object.keys(project.tags).length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Project Tags</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex flex-wrap gap-2">
                    {Object.entries(project.tags).map(([key, value]) => (
                      <Badge key={key} variant="secondary">
                        {key}: {value}
                      </Badge>
                    ))}
                  </div>
                </CardContent>
              </Card>
            )}
          </TabsContent>

          <TabsContent value="resources" className="space-y-4">
            <div className="flex justify-between items-center">
              <p className="text-sm text-muted-foreground">
                {resources.length} total resources
              </p>
              <Button>
                <Plus className="mr-2 h-4 w-4" />
                Assign Resource
              </Button>
            </div>

            {resources.length === 0 ? (
              <Card>
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <Server className="h-12 w-12 text-muted-foreground mb-4" />
                  <h3 className="text-lg font-semibold mb-2">
                    No resources assigned
                  </h3>
                  <p className="text-sm text-muted-foreground mb-4">
                    Assign resources to this project to get started
                  </p>
                  <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    Assign Resource
                  </Button>
                </CardContent>
              </Card>
            ) : (
              <Card>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Name</TableHead>
                      <TableHead>Type</TableHead>
                      <TableHead>Provider</TableHead>
                      <TableHead>Region</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Cost</TableHead>
                      <TableHead className="w-[50px]"></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {resources.map((resource) => (
                      <TableRow key={resource.id}>
                        <TableCell className="font-medium">
                          {resource.name}
                        </TableCell>
                        <TableCell>{resource.type}</TableCell>
                        <TableCell>
                          <Badge className={getProviderColor(resource.provider)}>
                            {resource.provider}
                          </Badge>
                        </TableCell>
                        <TableCell>{resource.region}</TableCell>
                        <TableCell>
                          <Badge className={getStatusColor(resource.status)}>
                            {resource.status}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          {resource.cost !== undefined 
                            ? `$${resource.cost.toFixed(2)}`
                            : "-"}
                        </TableCell>
                        <TableCell>
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button variant="ghost" size="icon">
                                <MoreVertical className="h-4 w-4" />
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                              <DropdownMenuLabel>Actions</DropdownMenuLabel>
                              <DropdownMenuSeparator />
                              <DropdownMenuItem>
                                View Details
                              </DropdownMenuItem>
                              <DropdownMenuItem className="text-destructive">
                                <Trash2 className="mr-2 h-4 w-4" />
                                Remove from Project
                              </DropdownMenuItem>
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </Card>
            )}
          </TabsContent>

          <TabsContent value="rules" className="space-y-4">
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <TrendingUp className="h-12 w-12 text-muted-foreground mb-4" />
                <h3 className="text-lg font-semibold mb-2">
                  No assignment rules
                </h3>
                <p className="text-sm text-muted-foreground mb-4 text-center max-w-md">
                  Create rules to automatically assign resources to this project based on tags
                </p>
                <Button>
                  <Plus className="mr-2 h-4 w-4" />
                  Create Rule
                </Button>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DashboardLayout>
  );
}

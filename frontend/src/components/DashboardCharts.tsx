"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart";
import { Skeleton } from "@/components/ui/skeleton";
import { Area, AreaChart, Bar, BarChart, Line, LineChart, Pie, PieChart, Cell, ResponsiveContainer, XAxis, YAxis, CartesianGrid, Legend } from "recharts";

interface ChartData {
  billingTrends: Array<{
    month: string;
    aws: number;
    azure: number;
    gcp: number;
    total: number;
  }>;
  resourceDistribution: Array<{
    provider: string;
    resources: number;
    color: string;
  }>;
  performanceMetrics: Array<{
    date: string;
    uptime: number;
    responseTime: number;
    errors: number;
  }>;
}

const chartConfig = {
  aws: {
    label: "AWS",
    color: "#FF9900",
  },
  azure: {
    label: "Azure",
    color: "#0078D4",
  },
  gcp: {
    label: "GCP",
    color: "#4285F4",
  },
  total: {
    label: "Total",
    color: "#8884d8",
  },
  uptime: {
    label: "Uptime %",
    color: "#82ca9d",
  },
  responseTime: {
    label: "Response Time (ms)",
    color: "#ffc658",
  },
  errors: {
    label: "Errors",
    color: "#ff7c7c",
  },
};

export function DashboardCharts() {
  const [chartData, setChartData] = useState<ChartData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchChartData = async () => {
      // TODO(graphql): Replace with a GraphQL query (e.g., dashboardCharts) once backend provides it.
      // For now this uses mock data to drive chart visuals.
      try {
        // TODO: Replace with actual backend API endpoint
        const response = await fetch('/api/dashboard/charts', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`, // TODO: Use proper auth context
          },
        });

        if (response.status === 401) {
          setError('Unauthorized access. Please log in.');
          return;
        }

        if (!response.ok) {
          throw new Error('Failed to fetch chart data');
        }

        const data = await response.json();
        setChartData(data);
      } catch (err) {
        console.error('Error fetching chart data:', err);
        // TODO: Mock data for development - remove in production
        setChartData({
          billingTrends: [
            { month: "Jan", aws: 1200, azure: 800, gcp: 600, total: 2600 },
            { month: "Feb", aws: 1350, azure: 950, gcp: 650, total: 2950 },
            { month: "Mar", aws: 1100, azure: 1200, gcp: 700, total: 3000 },
            { month: "Apr", aws: 1450, azure: 1100, gcp: 800, total: 3350 },
            { month: "May", aws: 1600, azure: 1300, gcp: 750, total: 3650 },
            { month: "Jun", aws: 1890, azure: 1650, gcp: 710, total: 4250 },
          ],
          resourceDistribution: [
            { provider: "AWS", resources: 342, color: "#FF9900" },
            { provider: "Azure", resources: 298, color: "#0078D4" },
            { provider: "GCP", resources: 207, color: "#4285F4" },
          ],
          performanceMetrics: [
            { date: "Mon", uptime: 99.9, responseTime: 120, errors: 2 },
            { date: "Tue", uptime: 99.8, responseTime: 135, errors: 5 },
            { date: "Wed", uptime: 100, responseTime: 110, errors: 0 },
            { date: "Thu", uptime: 99.7, responseTime: 150, errors: 8 },
            { date: "Fri", uptime: 99.9, responseTime: 125, errors: 3 },
            { date: "Sat", uptime: 100, responseTime: 105, errors: 1 },
            { date: "Sun", uptime: 99.8, responseTime: 130, errors: 4 },
          ],
        });
      } finally {
        setLoading(false);
      }
    };

    fetchChartData();
  }, []);

  if (loading) {
    return (
      <div className="grid gap-6 md:grid-cols-2">
        {Array.from({ length: 4 }).map((_, i) => (
          <Card key={i}>
            <CardHeader>
              <Skeleton className="h-6 w-48" />
              <Skeleton className="h-4 w-72" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-64 w-full" />
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <Card className="border-destructive/50">
        <CardHeader>
          <CardTitle className="text-destructive">Error Loading Charts</CardTitle>
          <CardDescription>{error}</CardDescription>
        </CardHeader>
      </Card>
    );
  }

  if (!chartData) return null;

  return (
    <div className="grid gap-6 md:grid-cols-2">
      {/* Billing Trends Chart */}
      <Card className="col-span-2">
        <CardHeader>
          <CardTitle>Billing Trends</CardTitle>
          <CardDescription>Monthly costs across cloud providers</CardDescription>
        </CardHeader>
        <CardContent>
          <ChartContainer config={chartConfig} className="h-64">
            <AreaChart data={chartData.billingTrends}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <ChartTooltip content={<ChartTooltipContent />} />
              <Area 
                type="monotone" 
                dataKey="aws" 
                stackId="1" 
                stroke={chartConfig.aws.color} 
                fill={chartConfig.aws.color} 
                fillOpacity={0.6}
              />
              <Area 
                type="monotone" 
                dataKey="azure" 
                stackId="1" 
                stroke={chartConfig.azure.color} 
                fill={chartConfig.azure.color} 
                fillOpacity={0.6}
              />
              <Area 
                type="monotone" 
                dataKey="gcp" 
                stackId="1" 
                stroke={chartConfig.gcp.color} 
                fill={chartConfig.gcp.color} 
                fillOpacity={0.6}
              />
              <Legend />
            </AreaChart>
          </ChartContainer>
        </CardContent>
      </Card>

      {/* Resource Distribution Chart */}
      <Card>
        <CardHeader>
          <CardTitle>Resource Distribution</CardTitle>
          <CardDescription>Resources by cloud provider</CardDescription>
        </CardHeader>
        <CardContent>
          <ChartContainer config={chartConfig} className="h-64">
            <PieChart>
              <Pie
                data={chartData.resourceDistribution}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={80}
                paddingAngle={5}
                dataKey="resources"
              >
                {chartData.resourceDistribution.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <ChartTooltip content={<ChartTooltipContent />} />
              <Legend />
            </PieChart>
          </ChartContainer>
        </CardContent>
      </Card>

      {/* Performance Metrics Chart */}
      <Card>
        <CardHeader>
          <CardTitle>Performance Metrics</CardTitle>
          <CardDescription>Weekly uptime and response time</CardDescription>
        </CardHeader>
        <CardContent>
          <ChartContainer config={chartConfig} className="h-64">
            <LineChart data={chartData.performanceMetrics}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <ChartTooltip content={<ChartTooltipContent />} />
              <Line 
                type="monotone" 
                dataKey="uptime" 
                stroke={chartConfig.uptime.color} 
                strokeWidth={2}
              />
              <Line 
                type="monotone" 
                dataKey="responseTime" 
                stroke={chartConfig.responseTime.color} 
                strokeWidth={2}
              />
              <Legend />
            </LineChart>
          </ChartContainer>
        </CardContent>
      </Card>
    </div>
  );
}
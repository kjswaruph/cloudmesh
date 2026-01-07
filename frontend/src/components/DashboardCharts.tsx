"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api-client";
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
      try {
        const credentials = await api.credentials.list();
        
        // Get last 6 months of data
        const endDate = new Date();
        const months = [];
        for (let i = 5; i >= 0; i--) {
          const date = new Date(endDate);
          date.setMonth(date.getMonth() - i);
          months.push({
            month: date.toLocaleDateString('en-US', { month: 'short' }),
            startDate: new Date(date.getFullYear(), date.getMonth(), 1).toISOString().split('T')[0],
            endDate: new Date(date.getFullYear(), date.getMonth() + 1, 0).toISOString().split('T')[0]
          });
        }

        // Fetch cost trends for all credentials by month
        const billingTrends = await Promise.all(
          months.map(async ({ month, startDate, endDate }) => {
            const monthData: any = { month, aws: 0, azure: 0, gcp: 0, total: 0 };
            
            await Promise.all(
              credentials.map(async (cred: any) => {
                try {
                  const costData = await api.costs.getCredentialCosts(cred.id, startDate, endDate);
                  const provider = (cred.provider || 'unknown').toLowerCase();
                  const cost = costData.totalCost || 0;
                  
                  if (provider === 'aws' || provider === 'azure' || provider === 'gcp') {
                    monthData[provider] += cost;
                  }
                  monthData.total += cost;
                } catch (err) {
                  console.warn(`Failed to fetch cost for ${cred.id}:`, err);
                }
              })
            );
            
            return monthData;
          })
        );

        // Calculate resource distribution by provider
        const providerResources = new Map<string, number>();
        credentials.forEach((cred: any) => {
          const provider = cred.provider || 'Unknown';
          const count = cred.resourceCount || 0;
          providerResources.set(provider, (providerResources.get(provider) || 0) + count);
        });

        const providerColors: Record<string, string> = {
          AWS: '#FF9900',
          Azure: '#0078D4',
          GCP: '#4285F4',
          DigitalOcean: '#0080FF'
        };

        const resourceDistribution = Array.from(providerResources.entries()).map(([provider, resources]) => ({
          provider,
          resources,
          color: providerColors[provider] || '#888888'
        }));

        // TODO: Performance metrics require backend health/monitoring endpoints
        const performanceMetrics = [
          { date: "Mon", uptime: 99.9, responseTime: 120, errors: 2 },
          { date: "Tue", uptime: 99.8, responseTime: 135, errors: 5 },
          { date: "Wed", uptime: 100, responseTime: 110, errors: 0 },
          { date: "Thu", uptime: 99.7, responseTime: 150, errors: 8 },
          { date: "Fri", uptime: 99.9, responseTime: 125, errors: 3 },
          { date: "Sat", uptime: 100, responseTime: 105, errors: 1 },
          { date: "Sun", uptime: 99.8, responseTime: 130, errors: 4 },
        ];

        setChartData({
          billingTrends,
          resourceDistribution,
          performanceMetrics
        });
      } catch (err) {
        console.error('Error fetching chart data:', err);
        setError(err instanceof Error ? err.message : 'Failed to load chart data');
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
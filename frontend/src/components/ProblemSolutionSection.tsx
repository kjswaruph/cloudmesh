import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";

export function ProblemSolutionSection() {
  return (
    <section id="problem-solution" className="w-full py-24 px-4">
      {/* Problem Statement */}
      <div className="max-w-6xl mx-auto mb-20">
        <div className="text-center mb-12">
          <Badge variant="destructive" className="mb-4 text-sm">🔥 THE PROBLEM</Badge>
          <h2 className="text-5xl font-bold mb-6 text-foreground">
            Managing Multiple Clouds Shouldn't Be This Hard
          </h2>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-12">
          <Card className="border-destructive/20">
            <CardHeader>
              <CardTitle className="text-destructive">Console Juggling Nightmare</CardTitle>
            </CardHeader>
            <CardContent>
              Switching between 3+ different cloud portals daily wastes hours of productive time
            </CardContent>
          </Card>
          
          <Card className="border-destructive/20">
            <CardHeader>
              <CardTitle className="text-destructive">Surprise Cloud Bills</CardTitle>
            </CardHeader>
            <CardContent>
              Unexpected charges from misconfigured resources across different platforms drain budgets
            </CardContent>
          </Card>
          
          <Card className="border-destructive/20">
            <CardHeader>
              <CardTitle className="text-destructive">Security Blind Spots</CardTitle>
            </CardHeader>
            <CardContent>
              Managing access controls and compliance across AWS, GCP, and Azure creates dangerous gaps
            </CardContent>
          </Card>
          
          <Card className="border-destructive/20">
            <CardHeader>
              <CardTitle className="text-destructive">Team Coordination Chaos</CardTitle>
            </CardHeader>
            <CardContent>
              No unified way to organize projects and resources for different teams and environments
            </CardContent>
          </Card>
        </div>

        {/* Statistics */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Alert className="border-destructive/20">
            <AlertDescription className="text-center">
              <span className="text-2xl font-bold text-destructive block">40%</span>
              Teams waste time switching between cloud consoles
            </AlertDescription>
          </Alert>
          
          <Alert className="border-destructive/20">
            <AlertDescription className="text-center">
              <span className="text-2xl font-bold text-destructive block">73%</span>
              Organizations experience unexpected cloud cost overruns
            </AlertDescription>
          </Alert>
          
          <Alert className="border-destructive/20">
            <AlertDescription className="text-center">
              <span className="text-2xl font-bold text-destructive block">45%</span>
              Multi-cloud security incidents increased in 2024
            </AlertDescription>
          </Alert>
        </div>
      </div>

      {/* Solution Statement */}
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-12">
          <Badge variant="default" className="mb-4 text-sm">✨ THE SOLUTION</Badge>
          <h2 className="text-5xl font-bold mb-6 text-foreground">
            One Dashboard. All Your Clouds. Complete Control.
          </h2>
          <p className="text-xl text-muted-foreground max-w-4xl mx-auto mb-8">
            Cloud Mesh eliminates cloud complexity by providing a single, unified interface to manage 
            all your cloud infrastructure. Save time, reduce costs, and enhance security across AWS, GCP, and Azure.
          </p>
          <Button size="lg" className="text-lg px-8 py-4" asChild>
            <Link href="/signup">Start Managing Smarter</Link>
          </Button>
        </div>
      </div>
    </section>
  );
}
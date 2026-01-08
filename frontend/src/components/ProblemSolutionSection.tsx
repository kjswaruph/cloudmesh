import Link from "next/link";
import { Button } from "@/components/ui/button";

export function ProblemSolutionSection() {
  return (
    <section id="problem-solution" className="w-full py-24 px-4">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-12">
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
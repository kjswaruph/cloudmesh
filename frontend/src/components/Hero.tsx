import Link from "next/link";
import { Button } from "@/components/ui/button";

export function Hero() {
  return (
    <section className="relative w-full min-h-screen flex items-end pb-24 pl-12">
      {/* Background for unicorn.studio interactive element */}
      <div className="absolute inset-0 -z-10" id="unicorn-bg">
        {/* Place unicorn.studio interactive background here */}
      </div>
      <div className="max-w-4xl">
        <h1 className="text-6xl md:text-7xl font-extrabold tracking-tight text-left mb-8 whitespace-pre-line leading-tight">
          MULTI-CLOUD MANAGEMENT.
        </h1>
        <Button className="text-lg px-8 py-4" variant="default" asChild>
          <Link href="/signup">Get Started</Link>
        </Button>
      </div>
    </section>
  );
}

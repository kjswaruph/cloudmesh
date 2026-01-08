"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api-client";
import { Navbar } from "@/components/Navbar";
import { Hero } from "@/components/Hero";
import { ProblemSolutionSection } from "@/components/ProblemSolutionSection";
import { FeaturesSection } from "@/components/FeaturesSection";
import { SupportedPlatformsSection } from "@/components/SupportedPlatformsSection";
import { Footer } from "@/components/Footer";

export default function Home() {
  const router = useRouter();

  useEffect(() => {
    let cancelled = false;
    const check = async () => {
      try {
        const user = await api.auth.me();
        if (!cancelled && user?.username) {
          router.replace('/dashboard');
        }
      } catch {
        // ignore
      }
    };
    check();
    return () => { cancelled = true; };
  }, [router]);

  return (
    <div className="relative min-h-screen font-sans overflow-hidden">
      {/* PatternCraft Light/Dark Background Layers */}
      <div className="pointer-events-none absolute inset-0 -z-10">
        {/* Light mode pattern */}
        <div
          className="absolute inset-0 opacity-100 dark:opacity-0 transition-opacity duration-500"
          style={{
            backgroundImage:
              "radial-gradient(125% 125% at 50% 90%, #ffffff 40%, #14b8a6 100%)",
            backgroundSize: "100% 100%",
          }}
        />
        {/* Dark mode pattern */}
        <div
          className="absolute inset-0 opacity-0 dark:opacity-100 transition-opacity duration-500"
          style={{
            background: "radial-gradient(125% 125% at 50% 100%, #000000 40%, #010133 100%)",
          }}
        />
        {/* Subtle overlay to ensure content contrast */}
        <div className="absolute inset-0 bg-background/40 backdrop-blur-[1px]" />
      </div>
      <Navbar />
      <Hero />
      <ProblemSolutionSection />
      <FeaturesSection />
      <SupportedPlatformsSection />
      <Footer />
    </div>
  );
}

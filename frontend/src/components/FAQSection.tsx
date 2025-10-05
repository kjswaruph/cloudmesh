import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from "@/components/ui/accordion";

export function FAQSection() {
  return (
    <section id="faq" className="w-full max-w-3xl mx-auto py-24 px-4">
      <h2 className="text-4xl font-bold mb-12 text-center">Frequently Asked Questions</h2>
      <Accordion type="single" collapsible className="space-y-4">
        <AccordionItem value="q1">
          <AccordionTrigger>What is Cloud Mesh?</AccordionTrigger>
          <AccordionContent>
            Cloud Mesh is a unified platform for managing multiple cloud providers from a single dashboard.
          </AccordionContent>
        </AccordionItem>
        <AccordionItem value="q2">
          <AccordionTrigger>Which cloud providers are supported?</AccordionTrigger>
          <AccordionContent>
            We support AWS, Azure, GCP, and more. Additional integrations are added regularly.
          </AccordionContent>
        </AccordionItem>
        <AccordionItem value="q3">
          <AccordionTrigger>Is there a free trial?</AccordionTrigger>
          <AccordionContent>
            Yes, we offer a 14-day free trial for all new users.
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </section>
  );
}

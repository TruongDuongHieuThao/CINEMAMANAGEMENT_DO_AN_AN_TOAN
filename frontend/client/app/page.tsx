import { Header } from "@/components/header"
import { HeroSection } from "@/components/hero-section"
import { NowShowingSection } from "@/components/now-showing-section"
import { ComingSoonSection } from "@/components/coming-soon-section"
import { FacilitiesSection } from "@/components/facilities-section"
import { TestimonialsSection } from "@/components/testimonials-section"
import { FAQSection } from "@/components/faq-section"

export default function Home() {
  return (
    <main className="min-h-screen bg-background">
      <HeroSection />
      <NowShowingSection />
      <ComingSoonSection />
      <FacilitiesSection />
      <TestimonialsSection />
      <FAQSection />
    </main>
  )
}

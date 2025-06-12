import type { Metadata } from "next";
import { Vazirmatn } from 'next/font/google';
import "./globals.css";

const vazir = Vazirmatn({
  variable: "--font-vazirmatn",
  subsets: ["latin", "arabic"],
});

export const metadata: Metadata = {
  title: "Polaris",
  description: "An app for mobile network monitoring + data aggregation and visualization",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="fa" dir="rtl">
      <body
        className={`${vazir.variable} antialiased`}
      >
        {children}
      </body>
    </html>
  );
}

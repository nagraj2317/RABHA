/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        rabha: {
          black: "#000000",
          surface: "#0A0A0C",
          high: "#141418",
          amber: "#FFB020",
          blue: "#00D1FF",
          purple: "#9B5CFF",
          text: "#F2F2F5",
          muted: "#9A9AA6",
          danger: "#FF5C7A",
          success: "#3DDC97",
        },
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
        mono: ["JetBrains Mono", "monospace"],
      },
    },
  },
  plugins: [],
};

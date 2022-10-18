/** @type {import('tailwindcss').Config} */
const colors = require('tailwindcss/colors')

module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: colors.pink,
        default: colors.stone,
        secondary: colors.yellow,
      },
    },
  },
  plugins: [],
}

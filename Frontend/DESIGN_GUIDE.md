# UI Design Guide

## Design Philosophy

This application follows a **modern SaaS product design** inspired by companies like Uber, Stripe, and Linear. The design emphasizes:

- **Minimalism**: Clean, uncluttered interfaces
- **Clarity**: Clear visual hierarchy and status indicators
- **Responsiveness**: Mobile-first approach
- **Accessibility**: Proper contrast ratios and focus states
- **Consistency**: Reusable components with consistent styling

## Color Palette

### Primary Colors (Green Theme)
```
Primary-50:  #f0fdf4  (Lightest - backgrounds)
Primary-100: #dcfce7
Primary-200: #bbf7d0
Primary-300: #86efac
Primary-400: #4ade80
Primary-500: #22c55e  (Brand color)
Primary-600: #16a34a  (Buttons, CTAs)
Primary-700: #15803d
Primary-800: #166534
Primary-900: #14532d  (Darkest)
```

### Status Colors

#### Success/Active (Green)
- **Use**: Accepted rides, completed actions, success messages
- **Class**: `text-green-600 bg-green-50 dark:bg-green-900/20`

#### Info/Assigned (Blue)
- **Use**: New assignments, informational states
- **Class**: `text-blue-600 bg-blue-50 dark:bg-blue-900/20`

#### Warning/Ongoing (Yellow)
- **Use**: In-progress rides, pending actions
- **Class**: `text-yellow-600 bg-yellow-50 dark:bg-yellow-900/20`

#### Error/Failed (Red)
- **Use**: Cancelled rides, errors, destructive actions
- **Class**: `text-red-600 bg-red-50 dark:bg-red-900/20`

#### Completed (Purple)
- **Use**: Finished rides awaiting payment
- **Class**: `text-purple-600 bg-purple-50 dark:bg-purple-900/20`

### Neutral Colors (Gray Scale)
```
Gray-50:  #f9fafb  (Backgrounds)
Gray-100: #f3f4f6
Gray-200: #e5e7eb  (Borders)
Gray-300: #d1d5db
Gray-400: #9ca3af  (Placeholders)
Gray-500: #6b7280  (Secondary text)
Gray-600: #4b5563
Gray-700: #374151  (Dark mode cards)
Gray-800: #1f2937  (Dark mode backgrounds)
Gray-900: #111827  (Darkest)
```

## Typography

### Font Stack
```css
font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', 
             Roboto, 'Helvetica Neue', Arial, sans-serif;
```

### Type Scale
- **Headings**: 
  - H1: `text-4xl font-bold` (36px)
  - H2: `text-2xl font-semibold` (24px)
  - H3: `text-lg font-medium` (18px)
  
- **Body**:
  - Large: `text-base` (16px)
  - Default: `text-sm` (14px)
  - Small: `text-xs` (12px)

## Component Styling Patterns

### Buttons

#### Primary Button
```jsx
className="px-6 py-3 bg-primary-600 hover:bg-primary-700 
           text-white font-medium rounded-lg 
           shadow-sm hover:shadow-md transition-all"
```

#### Secondary Button
```jsx
className="px-6 py-3 bg-gray-200 dark:bg-gray-700 
           hover:bg-gray-300 dark:hover:bg-gray-600 
           text-gray-900 dark:text-gray-100 font-medium 
           rounded-lg shadow-sm transition-all"
```

#### Danger Button
```jsx
className="px-6 py-3 bg-red-600 hover:bg-red-700 
           text-white font-medium rounded-lg 
           shadow-sm hover:shadow-md transition-all"
```

### Cards

#### Standard Card
```jsx
className="bg-white dark:bg-gray-800 rounded-xl shadow-sm 
           border border-gray-200 dark:border-gray-700 
           p-6 transition-all"
```

#### Hoverable Card
```jsx
className="bg-white dark:bg-gray-800 rounded-xl shadow-sm 
           border border-gray-200 dark:border-gray-700 
           hover:shadow-lg hover:border-gray-300 
           dark:hover:border-gray-600 p-6 transition-all cursor-pointer"
```

### Input Fields

#### Text Input
```jsx
className="w-full px-4 py-3 bg-white dark:bg-gray-700 
           border border-gray-300 dark:border-gray-600 
           rounded-lg focus:ring-2 focus:ring-primary-500 
           focus:border-transparent outline-none transition-all 
           placeholder:text-gray-400"
```

#### Input with Error
```jsx
className="w-full px-4 py-3 bg-white dark:bg-gray-700 
           border border-red-500 focus:ring-2 
           focus:ring-red-500 rounded-lg outline-none transition-all"
```

### Status Badges

#### Generic Badge
```jsx
className="inline-flex items-center gap-2 px-3 py-1.5 
           text-sm font-medium rounded-full uppercase 
           tracking-wide"
```

#### Success Badge
```jsx
className="text-green-600 bg-green-50 dark:bg-green-900/20"
```

## Spacing System

### Padding Scale
- `p-2`: 8px
- `p-3`: 12px
- `p-4`: 16px
- `p-6`: 24px
- `p-8`: 32px

### Margin Scale
- `m-2`: 8px
- `m-4`: 16px
- `m-6`: 24px
- `mt-8`: 32px top margin

### Gap Scale (Flexbox/Grid)
- `gap-2`: 8px
- `gap-3`: 12px
- `gap-4`: 16px
- `gap-6`: 24px

## Shadows

### Elevation Levels
```css
shadow-sm:  0 1px 2px 0 rgb(0 0 0 / 0.05)      /* Cards */
shadow:     0 1px 3px 0 rgb(0 0 0 / 0.1)       /* Default */
shadow-md:  0 4px 6px -1px rgb(0 0 0 / 0.1)    /* Hover states */
shadow-lg:  0 10px 15px -3px rgb(0 0 0 / 0.1)  /* Modals */
shadow-xl:  0 20px 25px -5px rgb(0 0 0 / 0.1)  /* Overlays */
```

## Border Radius

### Rounding Scale
- `rounded`: 4px (small elements)
- `rounded-lg`: 8px (buttons, inputs)
- `rounded-xl`: 12px (cards)
- `rounded-2xl`: 16px (large containers)
- `rounded-full`: 9999px (badges, avatars)

## Animations & Transitions

### Standard Transition
```jsx
className="transition-all duration-200"
```

### Hover Scale
```jsx
className="transform hover:scale-105 transition-transform"
```

### Pulse Animation
```jsx
className="animate-pulse"
```

### Custom Pulse (Slower)
```jsx
className="animate-pulse-slow"  // 3s duration
```

### Spin Animation (Loading)
```jsx
className="animate-spin"
```

## Responsive Breakpoints

### Tailwind Breakpoints
- `sm`: 640px and up
- `md`: 768px and up
- `lg`: 1024px and up
- `xl`: 1280px and up
- `2xl`: 1536px and up

### Usage Example
```jsx
className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4"
```

## Dark Mode Implementation

### Toggle Dark Mode
Dark mode is controlled by the `dark` class on the `<html>` element.

### Dark Mode Classes
```jsx
// Background
className="bg-white dark:bg-gray-800"

// Text
className="text-gray-900 dark:text-gray-100"

// Borders
className="border-gray-200 dark:border-gray-700"

// Hover states
className="hover:bg-gray-100 dark:hover:bg-gray-700"
```

## Accessibility Features

### Focus States
All interactive elements have visible focus outlines:
```jsx
className="focus:ring-2 focus:ring-primary-500 focus:ring-offset-2"
```

### Color Contrast
- Light mode: Text meets WCAG AA standards (4.5:1 ratio)
- Dark mode: Text meets WCAG AA standards (4.5:1 ratio)

### Semantic HTML
- Proper heading hierarchy (h1, h2, h3)
- Form labels associated with inputs
- ARIA labels where needed

## Layout Patterns

### Centered Container
```jsx
className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"
```

### Two-Column Layout
```jsx
className="grid grid-cols-1 lg:grid-cols-2 gap-6"
```

### Sticky Header
```jsx
className="sticky top-0 z-10"
```

### Full-Screen Layout
```jsx
className="min-h-screen"
```

## Icon Usage (Lucide React)

### Import Icons
```jsx
import { MapPin, User, DollarSign, Clock } from 'lucide-react'
```

### Icon Sizes
```jsx
// Small
<Icon className="w-4 h-4" />

// Medium (default)
<Icon className="w-5 h-5" />

// Large
<Icon className="w-8 h-8" />
```

### Icon with Color
```jsx
<MapPin className="w-5 h-5 text-primary-600" />
```

## Best Practices

### 1. Consistent Spacing
Use multiples of 4px (Tailwind's spacing scale)

### 2. Limit Color Usage
Stick to the defined color palette

### 3. Use Semantic Colors
- Green for success/positive actions
- Red for errors/destructive actions
- Blue for information
- Yellow for warnings

### 4. Maintain Visual Hierarchy
- Use size, weight, and color to establish hierarchy
- Most important elements should be largest/boldest

### 5. Keep It Simple
- Avoid over-decoration
- Use white space effectively
- One primary action per screen

### 6. Test Both Themes
Always test features in both light and dark mode

### 7. Mobile-First Approach
Design for mobile, then enhance for larger screens

## Component Examples

### Login Card
```jsx
<div className="w-full max-w-lg">
  <Card className="p-8">
    <h2 className="text-2xl font-semibold text-gray-900 dark:text-gray-100 text-center mb-6">
      Select Your Role
    </h2>
    {/* Content */}
  </Card>
</div>
```

### Status Progress Bar
```jsx
<div className="flex items-center justify-between">
  <div className="w-8 h-8 rounded-full bg-primary-600 text-white 
                  flex items-center justify-center text-xs font-medium">
    1
  </div>
  <div className="h-1 w-12 bg-primary-600" />
  <div className="w-8 h-8 rounded-full bg-gray-200 text-gray-500 
                  flex items-center justify-center text-xs font-medium">
    2
  </div>
</div>
```

### Stats Card
```jsx
<div className="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg 
                border border-gray-200 dark:border-gray-600">
  <div className="inline-flex p-2 rounded-lg bg-green-50 dark:bg-green-900/20 mb-3">
    <DollarSign className="w-5 h-5 text-green-600" />
  </div>
  <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">
    Total Earnings
  </p>
  <p className="text-lg font-bold text-gray-900 dark:text-gray-100">
    $1,250.00
  </p>
</div>
```

## Performance Considerations

### CSS Purging
Tailwind automatically removes unused CSS in production builds.

### Class Merging
Use consistent class order for better performance:
1. Layout (display, position)
2. Spacing (margin, padding)
3. Sizing (width, height)
4. Typography (font, text)
5. Visual (background, border)
6. Effects (shadow, opacity)
7. Transitions/Animations

### Example
```jsx
className="flex items-center gap-4 px-6 py-3 w-full 
           text-sm font-medium bg-primary-600 rounded-lg 
           shadow-sm hover:shadow-md transition-all"
```

This design system ensures a consistent, modern, and professional user experience across the entire application! 🎨

import '@mdi/font/css/materialdesignicons.css'

import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import { pl } from 'vuetify/locale'

import { VDateInput } from 'vuetify/labs/VDateInput'

const myCustomLightTheme = {
  dark: false,
  colors: {
    background: '#faf0e6',
    surface: '#faf0e6',
    hover: '#e4d5b7'
  },
  variables: {
    'disabled-opacity': 0.6,
  }
}
export default defineNuxtPlugin((app) => {
  const vuetify = createVuetify({
    ssr: true,
    components: {
      VDateInput,
    },
    locale: {
      locale: 'pl',
      fallback: 'pl',
      messages: { pl },
    },
    theme: {
      defaultTheme: 'myCustomLightTheme',
      themes: {
        myCustomLightTheme
      }
    }
  })
  app.vueApp.use(vuetify)
})
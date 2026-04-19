import axios from 'axios'

const api = axios.create({
  baseURL: '/api/admin',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const routesApi = {
  list: () => api.get('/routes'),
  get: (id) => api.get(`/routes/${id}`),
  create: (data) => api.post('/routes', data),
  update: (id, data) => api.put(`/routes/${id}`, data),
  delete: (id) => api.delete(`/routes/${id}`)
}

export const servicesApi = {
  list: () => api.get('/services'),
  get: (id) => api.get(`/services/${id}`),
  create: (data) => api.post('/services', data),
  update: (id, data) => api.put(`/services/${id}`, data),
  delete: (id) => api.delete(`/services/${id}`),
  instances: {
    list: (serviceId) => api.get(`/services/${serviceId}/instances`),
    create: (serviceId, data) => api.post(`/services/${serviceId}/instances`, data),
    update: (serviceId, id, data) => api.put(`/services/${serviceId}/instances/${id}`, data),
    delete: (serviceId, id) => api.delete(`/services/${serviceId}/instances/${id}`)
  }
}

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  apiKeys: {
    list: () => api.get('/api-keys'),
    create: (data) => api.post('/api-keys', data),
    update: (id, data) => api.put(`/api-keys/${id}`, data),
    delete: (id) => api.delete(`/api-keys/${id}`)
  },
  jwt: {
    get: () => api.get('/jwt-config'),
    update: (data) => api.put('/jwt-config', data)
  }
}

export const rateLimitApi = {
  list: () => api.get('/rate-limit-rules'),
  create: (data) => api.post('/rate-limit-rules', data),
  update: (id, data) => api.put(`/rate-limit-rules/${id}`, data),
  delete: (id) => api.delete(`/rate-limit-rules/${id}`)
}

export const blacklistApi = {
  list: () => api.get('/blacklist'),
  create: (data) => api.post('/blacklist', data),
  delete: (id) => api.delete(`/blacklist/${id}`)
}

export const whitelistApi = {
  list: () => api.get('/whitelist'),
  create: (data) => api.post('/whitelist', data),
  delete: (id) => api.delete(`/whitelist/${id}`)
}

export const logsApi = {
  list: (params) => api.get('/logs', { params }),
  export: (params) => api.get('/logs/export', { params, responseType: 'blob' })
}

export default api

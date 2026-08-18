import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  console.log('API 请求:', {
    method: config.method,
    url: config.url,
    params: config.params
  })
  return config
})

api.interceptors.response.use(
  res => {
    const responseData = res.data
    console.log('API 响应详情:', {
      url: res.config.url,
      status: res.status,
      data: responseData,
      dataType: typeof responseData,
      hasCode: responseData?.code !== undefined,
      codeValue: responseData?.code,
      hasData: responseData?.data !== undefined
    })
    // 后端统一返回 Result 结构：{code, message, data}
    // 如果 response.data 存在 code 字段，说明是后端的 Result 对象
    if (responseData && typeof responseData.code === 'number') {
      console.log('检测到 Result 结构，code:', responseData.code)
      // 修改：接受 code 为 0 或 200 都表示成功
      if (responseData.code !== 0 && responseData.code !== 200) {
        // 业务错误
        console.log('业务错误，reject:', responseData)
        return Promise.reject(responseData)
      }
      // 返回 Result 中的 data 字段
      const result = responseData.data !== undefined ? responseData.data : {}
      console.log('返回 data 字段:', result)
      return result
    }
    // 否则直接返回 response.data
    console.log('直接返回 response.data:', responseData)
    return responseData
  },
  err => {
    console.error('API 错误详情:', {
      message: err.message,
      status: err.response?.status,
      data: err.response?.data,
      code: err.code
    })
    if (err.response?.status === 401) {
      // 只在访问需要登录的页面时才跳转到登录页
      const needAuthPaths = ['/cart', '/order', '/user', '/merchant', '/admin']
      const currentHash = location.hash
      const shouldRedirect = needAuthPaths.some(path => currentHash.includes(path))
      
      if (shouldRedirect) {
        console.log('访问需要登录的页面，跳转到登录页')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        if (!currentHash.includes('login') && !currentHash.includes('register')) {
          location.href = '/#/login'
        }
      } else {
        console.log('公开接口 401 错误，不跳转，继续访问')
      }
    }
    return Promise.reject(err.response?.data || err.message)
  }
)

export default api

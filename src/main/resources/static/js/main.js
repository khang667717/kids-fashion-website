// ===== MAIN.JS - Kids Fashion Store =====
// Tất cả các chức năng JavaScript cho website

// ===== OVERRIDE WINDOW.ADDTOCART - ĐẶT Ở ĐẦU FILE =====
window.addToCart = async function(productId, quantity, size) {
    let buttonElement = null;
    let originalButtonHtml = '';

    // Xử lý nếu gọi từ nút có sẵn (trang chi tiết)
    if (productId && productId instanceof Event) {
        productId.preventDefault();
        buttonElement = productId.currentTarget;
        if (buttonElement && !buttonElement.disabled) {
            productId = buttonElement.getAttribute('data-product-id');
            const qtyInput = document.getElementById('quantity');
            if (qtyInput) quantity = qtyInput.value;
            const sizeInput = document.querySelector('input[name="size"]:checked');
            if (sizeInput) size = sizeInput.value;

            originalButtonHtml = buttonElement.innerHTML;
            buttonElement.disabled = true;
            buttonElement.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
        }
    }

    const numProductId = parseInt(productId);
    const numQuantity = parseInt(quantity) || 1;

    if (isNaN(numProductId) || numProductId <= 0) {
        if(buttonElement) {
            buttonElement.disabled = false;
            buttonElement.innerHTML = originalButtonHtml;
        }
        return false;
    }

    const result = await CartManager.addToCart(numProductId, numQuantity, size);

    if (buttonElement) {
        buttonElement.disabled = false;
        buttonElement.innerHTML = originalButtonHtml;
    }
    
    return result;
};

// ===== CẤU HÌNH =====
const CONFIG = {
    ANIMATION_DURATION: 300,
    TOAST_DURATION: 3000,
    API_ENDPOINTS: {
        CART_SIZE: '/cart/api/cart/size',
        CATEGORIES: '/categories/api/categories',
        CART_ADD: '/cart/add',
        CART_UPDATE: '/cart/update',
        CART_REMOVE: '/cart/remove',
        APPLY_COUPON: '/cart/apply-coupon',
        REMOVE_COUPON: '/cart/remove-coupon'
    }
};

// ===== CSRF TOKEN HANDLER =====
const CsrfHandler = {
    getToken() {
        return {
            token: document.querySelector('meta[name="_csrf"]')?.content,
            header: document.querySelector('meta[name="_csrf_header"]')?.content
        };
    },

    addToHeaders(options = {}) {
        const csrf = this.getToken();
        console.log('CSRF Token:', csrf);

        if (csrf.token && csrf.header) {
            options.headers = options.headers || {};
            options.headers[csrf.header] = csrf.token;
            console.log('Added CSRF header:', csrf.header, csrf.token);
        } else {
            console.warn('CSRF token not found!');
        }
        return options;
    }
};

// ===== FETCH WRAPPER =====
async function fetchWithCsrf(url, options = {}) {
    try {
        options = CsrfHandler.addToHeaders(options);

        if (!url.startsWith('http') && !url.startsWith('/')) {
            url = '/' + url;
        }

        console.log('Sending request to:', url);
        console.log('With options:', options);

        const response = await fetch(url, options);

        if (!response.ok) {
            console.error('HTTP error:', response.status);
            if (response.status === 302) {
                throw new Error('Session expired. Please login again.');
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            const text = await response.text();
            console.error('Response is not JSON:', text.substring(0, 200));
            throw new Error('Server returned HTML instead of JSON');
        }

        return await response.json();
    } catch (error) {
        console.error('Fetch error:', error);
        throw error;
    }
}

// ===== NOTIFICATION SYSTEM =====
const Notification = {
    show(message, type = 'info') {
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            // Căn lề phải, dưới thanh navbar header (top down 75px)
            container.className = 'toast-container position-fixed end-0 p-3 me-2 mt-2';
            container.style.top = '75px';
            container.style.zIndex = '9999';
            document.body.appendChild(container);
        }

        const toastId = 'toast-' + Date.now();
        const toast = document.createElement('div');
        toast.id = toastId;
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'assertive');
        toast.setAttribute('aria-atomic', 'true');

        // Thời gian tuỳ chỉnh: 3s cho success, 5s cho lỗi
        const duration = type === 'success' ? 3000 : (type === 'danger' ? 5000 : 4000);

        // Class CSS nâng cao (kết nối với style.css)
        toast.className = `toast toast-${type} align-items-center border-0`;

        toast.innerHTML = `
            <div class="d-flex px-2 py-2 position-relative">
                <div class="toast-body fw-semibold d-flex align-items-center">
                    <i class="fas ${this.getIcon(type)} me-2" style="font-size: 1.25rem;"></i>
                    <span style="font-size: 0.95rem; letter-spacing: 0.02em;">${message}</span>
                </div>
                <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast"></button>
                <div class="toast-progress"></div>
            </div>
        `;

        container.appendChild(toast);

        const bsToast = new bootstrap.Toast(toast, {
            animation: true,
            autohide: true,
            delay: duration
        });
        
        bsToast.show();

        // Xử lý animation cho thanh progress bar
        const progress = toast.querySelector('.toast-progress');
        if (progress) {
            progress.style.width = '100%';
            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    progress.style.transition = `width ${duration}ms linear`;
                    progress.style.width = '0%';
                });
            });
        }

        toast.addEventListener('hidden.bs.toast', () => {
            toast.remove();
        });
    },

    getIcon(type) {
        const icons = {
            success: 'fa-check-circle',
            danger: 'fa-exclamation-circle',
            warning: 'fa-exclamation-triangle',
            info: 'fa-info-circle'
        };
        return icons[type] || icons.info;
    },

    success(message) { this.show(message, 'success'); },
    error(message) { this.show(message, 'danger'); },
    warning(message) { this.show(message, 'warning'); },
    info(message) { this.show(message, 'info'); }
};

// ===== LOADING SPINNER =====
const LoadingSpinner = {
    show(selector = 'body') {
        const target = document.querySelector(selector);
        if (!target) return;

        const spinner = document.createElement('div');
        spinner.className = 'spinner-overlay position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center';
        spinner.style.backgroundColor = 'rgba(255,255,255,0.8)';
        spinner.style.zIndex = '99999';
        spinner.innerHTML = '<div class="spinner"></div>';

        target.appendChild(spinner);
        return spinner;
    },

    hide(spinner) {
        if (spinner) {
            spinner.style.opacity = '0';
            setTimeout(() => spinner.remove(), 300);
        }
    }
};

// ===== CART MANAGER =====
const CartManager = {
    async getSize() {
        try {
            const data = await fetchWithCsrf(CONFIG.API_ENDPOINTS.CART_SIZE);
            return data.cartSize || 0;
        } catch (error) {
            console.error('Error fetching cart size:', error);
            return 0;
        }
    },

    async updateCount() {
        const size = await this.getSize();
        const cartCount = document.getElementById('cartCount');
        if (cartCount) {
            cartCount.innerText = size;
            this.animateCartIcon();
        }
        return size;
    },

    animateCartIcon() {
        const cartIcon = document.querySelector('.fa-shopping-cart');
        if (cartIcon) {
            cartIcon.classList.add('animate__animated', 'animate__rubberBand');
            setTimeout(() => {
                cartIcon.classList.remove('animate__animated', 'animate__rubberBand');
            }, 1000);
        }
    },

    async addToCart(productId, quantity, size) {
        console.log('CartManager.addToCart received:', {productId, quantity, size});

        if (!productId || isNaN(parseInt(productId))) {
            console.error('Invalid productId in CartManager:', productId);
            Notification.error('Product information is invalid');
            return false;
        }

        const params = new URLSearchParams({
            productId: productId,
            quantity: quantity,
            size: size   // ✅ Thêm size
        });

        console.log('Sending to server:', {
            url: CONFIG.API_ENDPOINTS.CART_ADD,
            params: params.toString()
        });

        try {
            const data = await fetchWithCsrf(CONFIG.API_ENDPOINTS.CART_ADD, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: params.toString()
            });

            console.log('Server response:', data);

            if (data.success) {
                await this.updateCount();

                Notification.success('Added to Cart Successfully!');

                document.dispatchEvent(new CustomEvent('cart:updated', {
                    detail: { cartSize: data.cartSize, total: data.total }
                }));

                return true;
            } else {
                Notification.error(data.message || 'Error adding to cart');
                return false;
            }
        } catch (error) {
            console.error('Error:', error);
            Notification.error('Cannot add product to cart. Please try again.');
            return false;
        }
    }
};

// ===== CATEGORY MANAGER =====
const CategoryManager = {
    async loadCategories() {
        const menu = document.getElementById('categoryMenu');
        if (!menu) return;

        try {
            const categories = await fetchWithCsrf(CONFIG.API_ENDPOINTS.CATEGORIES);

            menu.innerHTML = '';

            if (categories.length === 0) {
                menu.innerHTML = '<li><a class="dropdown-item disabled" href="#">No categories</a></li>';
                return;
            }

            categories.forEach(cat => {
                const li = document.createElement('li');
                li.innerHTML = `
                    <a class="dropdown-item" href="/products?category=${cat.id}">
                        <i class="fas fa-tag me-2" style="color: var(--primary-color);"></i>
                        ${cat.name}
                    </a>
                `;
                menu.appendChild(li);
            });

        } catch (error) {
            console.error('Error loading categories:', error);
            menu.innerHTML = '<li><a class="dropdown-item disabled" href="#">Error loading categories</a></li>';
        }
    }
};

// ===== SEARCH HANDLER =====
const SearchHandler = {
    init() {
        const searchForm = document.querySelector('form[action="/search"]');
        const searchBtn = document.getElementById('magicSearchBtn');
        const searchInput = document.getElementById('magicSearchInput');

        // Logic cũ (chặn submit nếu trống)
        if (searchForm && !searchBtn) {
            searchForm.addEventListener('submit', (e) => {
                const input = searchForm.querySelector('input[name="keyword"]');
                if (input && !input.value.trim()) {
                    e.preventDefault();
                    Notification.warning('Please enter a search term');
                }
            });
        }

        // Logic mới (Expandable Search bar)
        if (searchBtn && searchInput && searchForm) {
            // Ngăn form submit mặc định để xử lý hiệu ứng
            searchForm.addEventListener('submit', (e) => {
                if (!searchInput.value.trim()) {
                    e.preventDefault();
                    Notification.warning('Please enter a search term');
                }
            });

            searchBtn.addEventListener('click', function(e) {
                if (!searchInput.classList.contains('expanded')) {
                    // Trạng thái ĐÓNG: Bấm vào icon thì MỞ RỘNG
                    e.preventDefault();
                    searchInput.classList.add('expanded');
                    searchInput.focus();
                } else if (!searchInput.value.trim()) {
                    // Trạng thái MỞ, trống chữ: Bấm vào icon thì THU LẠI
                    e.preventDefault();
                    searchInput.classList.remove('expanded');
                } else {
                    // Trạng thái MỞ, có chữ: Bấm vào icon thì TÌM KIẾM
                    searchForm.submit();
                }
            });

            // Khi click ra ngoài thì thu lại nếu ô đang trống
            document.addEventListener('click', function(e) {
                if (!searchInput.contains(e.target) && !searchBtn.contains(e.target)) {
                    if (!searchInput.value.trim()) {
                        searchInput.classList.remove('expanded');
                    }
                }
            });
        }
    }
};

// ===== ANIMATION ON SCROLL =====
const ScrollAnimation = {
    init() {
        const animatedElements = document.querySelectorAll('.card, .dashboard-card, .hero');

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('fade-in');
                    observer.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        });

        animatedElements.forEach(el => observer.observe(el));
    }
};

// ===== PRICE FORMATTER =====
const PriceFormatter = {
    format(price) {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(price);
    },

    formatAll() {
        document.querySelectorAll('.format-price').forEach(el => {
            const price = parseFloat(el.dataset.price || el.innerText.replace(/[^0-9.-]+/g, ''));
            if (!isNaN(price)) {
                el.innerText = this.format(price);
            }
        });
    }
};

// ===== INITIALIZATION =====
document.addEventListener('DOMContentLoaded', async () => {
    const spinner = LoadingSpinner.show();

    try {
        await Promise.all([
            CartManager.updateCount(),
            CategoryManager.loadCategories()
        ]);

        SearchHandler.init();
        ScrollAnimation.init();
        PriceFormatter.formatAll();

        // Removed dynamic inline button hover transitions to allow smooth CSS handling.

        console.log('✨ Kids Fashion Store - Website loaded successfully!');

    } catch (error) {
        console.error('Error during initialization:', error);
        Notification.error('Error loading website. Please refresh the page.');
    } finally {
        LoadingSpinner.hide(spinner);
    }
});

// ===== CART PAGE SPECIFIC =====
if (window.location.pathname.includes('/cart')) {
    document.querySelectorAll('.cart-quantity').forEach(input => {
        let timeoutId;

        input.addEventListener('input', function() {
            clearTimeout(timeoutId);

            const productId = this.dataset.productId;
            const quantity = parseInt(this.value);

            if (quantity < 1) {
                this.value = 1;
                return;
            }

            timeoutId = setTimeout(async () => {
                const params = new URLSearchParams({
                    productId: productId,
                    quantity: quantity
                });

                try {
                    const data = await fetchWithCsrf('/cart/update', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                        },
                        body: params.toString()
                    });

                    if (data.success) {
                        Notification.success('Cart updated!');
                        await CartManager.updateCount();
                    } else {
                        alert(data.message);
                    }
                } catch (error) {
                    console.error('Error updating cart:', error);
                    Notification.error('Error updating cart: ' + error.message);
                }
            }, 800);
        });
    });
}

// ===== REMOVE COUPON FUNCTION =====
window.removeCoupon = async function() {
    try {
        const data = await fetchWithCsrf('/cart/remove-coupon', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });

        if (data.success) {
            Notification.success('Coupon removed!');
            setTimeout(() => location.reload(), 1000);
        } else {
            Notification.error(data.message);
        }
    } catch (error) {
        console.error('Error removing coupon:', error);
        Notification.error('Error removing coupon: ' + error.message);
    }
};
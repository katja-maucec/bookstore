import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'Authorities' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'book',
    data: { pageTitle: 'Books' },
    loadChildren: () => import('./book/book.routes'),
  },
  {
    path: 'category',
    data: { pageTitle: 'Categories' },
    loadChildren: () => import('./category/category.routes'),
  },
  {
    path: 'review',
    data: { pageTitle: 'Reviews' },
    loadChildren: () => import('./review/review.routes'),
  },
  {
    path: 'shopping-cart',
    data: { pageTitle: 'ShoppingCarts' },
    loadChildren: () => import('./shopping-cart/shopping-cart.routes'),
  },
  {
    path: 'cart-item',
    data: { pageTitle: 'CartItems' },
    loadChildren: () => import('./cart-item/cart-item.routes'),
  },
  {
    path: 'order',
    data: { pageTitle: 'Orders' },
    loadChildren: () => import('./order/order.routes'),
  },
  {
    path: 'order-item',
    data: { pageTitle: 'OrderItems' },
    loadChildren: () => import('./order-item/order-item.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
